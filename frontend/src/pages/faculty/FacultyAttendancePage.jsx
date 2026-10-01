import React, { useState, useEffect } from 'react';
import { attendanceService } from '../../services/attendanceService';
import { enrollmentService } from '../../services/enrollmentService';
import { dashboardService } from '../../services/dashboardService';
import { courseService } from '../../services/courseService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import StatusBadge, { CourseTypeBadge, SectionBadge } from '../../components/common/Badge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import {
  FiCheckSquare,
  FiList,
  FiSave,
  FiCheck,
  FiX,
  FiEdit2,
  FiTrash2,
  FiUsers,
  FiCheckCircle,
  FiAlertCircle,
  FiRefreshCw
} from 'react-icons/fi';

export const FacultyAttendancePage = () => {
  const [activeTab, setActiveTab] = useState('mark'); // 'mark' | 'history'
  const [courses, setCourses] = useState([]);
  const [selectedCourseKey, setSelectedCourseKey] = useState('');
  const [attendanceDate, setAttendanceDate] = useState(() => {
    // Current date in YYYY-MM-DD
    const d = new Date();
    return d.toISOString().split('T')[0];
  });

  const getCourseKey = (c) => `${c.courseId}___${c.section || 'A'}`;
  const parseCourseKey = (key) => {
    if (!key) return { courseId: null, section: null };
    const [cId, sec] = key.split('___');
    return { courseId: Number(cId), section: sec || null };
  };

  // Batch Marking state
  const [students, setStudents] = useState([]);
  const [attendanceMap, setAttendanceMap] = useState({}); // { [studentId]: { status: 'PRESENT'|'ABSENT', remarks: '' } }
  const [rosterLoading, setRosterLoading] = useState(false);
  const [rosterError, setRosterError] = useState(null);
  const [saveLoading, setSaveLoading] = useState(false);
  const [saveSuccessMsg, setSaveSuccessMsg] = useState('');
  const [saveErrorMsg, setSaveErrorMsg] = useState('');

  // History Tab state
  const [records, setRecords] = useState([]);
  const [historyCourseKey, setHistoryCourseKey] = useState('');
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState(null);
  const [historyPage, setHistoryPage] = useState(0);
  const [historyTotalPages, setHistoryTotalPages] = useState(1);
  const [historyTotalElements, setHistoryTotalElements] = useState(0);

  // Edit Modal in History
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [editStatus, setEditStatus] = useState('PRESENT');
  const [editRemarks, setEditRemarks] = useState('');
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState('');

  // Delete Dialog in History
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // 1. Load faculty courses on mount
  useEffect(() => {
    courseService.getFacultyCourses()
      .then((res) => {
        const list = res.data || [];
        setCourses(list);
        if (list.length > 0) {
          const firstKey = getCourseKey(list[0]);
          setSelectedCourseKey(firstKey);
          setHistoryCourseKey(firstKey);
        }
      })
      .catch(() => {
        // Fallback to dashboardService if courseService fails
        dashboardService.getFacultyDashboard()
          .then((dRes) => {
            const list = dRes.data?.assignedCourses || [];
            setCourses(list);
            if (list.length > 0) {
              const firstKey = getCourseKey(list[0]);
              setSelectedCourseKey(firstKey);
              setHistoryCourseKey(firstKey);
            }
          })
          .catch(() => {});
      });
  }, []);

  // 2. Load enrolled students and existing attendance for batch marking
  const loadRosterAndAttendance = async (courseKey, date) => {
    const { courseId, section } = parseCourseKey(courseKey);
    if (!courseId) {
      setStudents([]);
      setAttendanceMap({});
      return;
    }
    setRosterLoading(true);
    setRosterError(null);
    setSaveSuccessMsg('');
    setSaveErrorMsg('');

    try {
      // Fetch enrolled students scoped strictly by course and section
      const enrollRes = await enrollmentService.getFacultyEnrollments({ courseId, section, size: 200 });
      const enrolled = enrollRes.data?.content || [];
      setStudents(enrolled);

      // Fetch any existing attendance for this course, section and date
      let existingRecords = [];
      try {
        const attRes = await attendanceService.getFacultyAttendance({ courseId, section, size: 200 });
        const allAtt = attRes.data?.content || [];
        existingRecords = allAtt.filter((a) => a.attendanceDate === date);
      } catch (e) {
        // non-fatal, start fresh
      }

      // Build map
      const initialMap = {};
      enrolled.forEach((st) => {
        const found = existingRecords.find((rec) => rec.studentId === st.studentId || rec.studentRollNumber === st.studentRollNumber);
        if (found) {
          initialMap[st.studentId] = {
            status: found.status || 'PRESENT',
            remarks: found.remarks || '',
          };
        } else {
          initialMap[st.studentId] = {
            status: 'PRESENT', // default to Present
            remarks: '',
          };
        }
      });
      setAttendanceMap(initialMap);
    } catch (err) {
      setRosterError(err.response?.data?.message || err.message || 'Failed to load enrolled students.');
    } finally {
      setRosterLoading(false);
    }
  };

  useEffect(() => {
    if (selectedCourseKey) {
      loadRosterAndAttendance(selectedCourseKey, attendanceDate);
    }
  }, [selectedCourseKey, attendanceDate]);

  // Bulk actions
  const handleMarkAll = (status) => {
    setAttendanceMap((prev) => {
      const updated = { ...prev };
      students.forEach((st) => {
        updated[st.studentId] = {
          ...updated[st.studentId],
          status,
        };
      });
      return updated;
    });
  };

  const handleStudentStatusChange = (studentId, status) => {
    setAttendanceMap((prev) => ({
      ...prev,
      [studentId]: {
        ...prev[studentId],
        status,
      },
    }));
  };

  const handleStudentRemarksChange = (studentId, remarks) => {
    setAttendanceMap((prev) => ({
      ...prev,
      [studentId]: {
        ...prev[studentId],
        remarks,
      },
    }));
  };

  // Submit batch attendance
  const handleSaveAttendance = async () => {
    const { courseId } = parseCourseKey(selectedCourseKey);
    if (!courseId || !attendanceDate || students.length === 0) return;

    setSaveLoading(true);
    setSaveSuccessMsg('');
    setSaveErrorMsg('');

    try {
      const recordsToSave = students.map((st) => {
        const item = attendanceMap[st.studentId] || { status: 'PRESENT', remarks: '' };
        return {
          studentId: st.studentId,
          status: item.status,
          remarks: item.remarks?.trim() || null,
        };
      });

      await attendanceService.recordBatchAttendance({
        courseId: Number(courseId),
        attendanceDate: attendanceDate,
        records: recordsToSave,
      });

      setSaveSuccessMsg(
        `Attendance successfully saved for ${recordsToSave.length} students on ${attendanceDate}!`
      );
      // Reload roster and refresh history tab
      loadRosterAndAttendance(selectedCourseKey, attendanceDate);
      if (activeTab === 'history') {
        fetchHistory(historyPage, historyCourseKey);
      }
    } catch (err) {
      setSaveErrorMsg(err.response?.data?.message || err.message || 'Failed to save batch attendance.');
    } finally {
      setSaveLoading(false);
    }
  };

  // 3. Load attendance history
  const fetchHistory = async (pageNumber = 0, courseKey = historyCourseKey) => {
    setHistoryLoading(true);
    setHistoryError(null);
    try {
      const { courseId, section } = parseCourseKey(courseKey);
      const params = { page: pageNumber, size: 20 };
      if (courseId) params.courseId = courseId;
      if (section) params.section = section;
      const res = await attendanceService.getFacultyAttendance(params);
      const pageData = res.data;
      setRecords(pageData?.content || []);
      setHistoryPage(pageData?.pageNumber || 0);
      setHistoryTotalPages(pageData?.totalPages || 1);
      setHistoryTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setHistoryError(err.response?.data?.message || err.message || 'Failed to load attendance history.');
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'history') {
      fetchHistory(0, historyCourseKey);
    }
  }, [activeTab, historyCourseKey]);

  // Edit / Delete handlers for history
  const handleOpenEdit = (rec) => {
    setEditingRecord(rec);
    setEditStatus(rec.status);
    setEditRemarks(rec.remarks || '');
    setEditError('');
    setIsEditModalOpen(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditLoading(true);
    setEditError('');
    try {
      await attendanceService.updateFacultyAttendance(editingRecord.attendanceId, {
        status: editStatus,
        remarks: editRemarks.trim() || undefined,
      });
      setIsEditModalOpen(false);
      fetchHistory(historyPage, historyCourseId);
    } catch (err) {
      setEditError(err.response?.data?.message || err.message || 'Failed to update attendance.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await attendanceService.deleteFacultyAttendance(deletingId);
      setIsDeleteDialogOpen(false);
      fetchHistory(historyPage, historyCourseId);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete attendance record.');
    } finally {
      setDeleteLoading(false);
    }
  };

  // Calculated attendance stats for current batch
  const totalStudentsCount = students.length;
  const presentCount = students.filter((s) => (attendanceMap[s.studentId]?.status || 'PRESENT') === 'PRESENT').length;
  const absentCount = totalStudentsCount - presentCount;
  const attendanceRate = totalStudentsCount > 0 ? Math.round((presentCount / totalStudentsCount) * 100) : 0;

  const selectedCourseObj = courses.find((c) => getCourseKey(c) === selectedCourseKey);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Attendance Management"
        subtitle="Record daily course attendance by roster or review historical logs"
      >
        <div className="flex items-center gap-2 bg-theme-surface p-1 rounded-xl border border-theme">
          <button
            type="button"
            onClick={() => setActiveTab('mark')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'mark'
                ? 'bg-theme-brand text-white shadow-sm'
                : 'text-theme-secondary hover:text-theme-primary'
            }`}
          >
            <FiCheckSquare className="w-3.5 h-3.5" />
            Mark Roster Attendance
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('history')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'history'
                ? 'bg-theme-brand text-white shadow-sm'
                : 'text-theme-secondary hover:text-theme-primary'
            }`}
          >
            <FiList className="w-3.5 h-3.5" />
            Attendance Logs & History
          </button>
        </div>
      </PageHeader>

      {/* ================= TAB 1: MARK ROSTER ATTENDANCE ================= */}
      {activeTab === 'mark' && (
        <div className="space-y-6">
          {/* Controls Bar */}
          <div className="bg-theme-surface p-5 rounded-2xl border border-theme shadow-sm space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-theme-secondary mb-1.5">
                  Select Assigned Course & Section <span className="text-red-500">*</span>
                </label>
                <Select
                  value={selectedCourseKey}
                  onChange={(e) => setSelectedCourseKey(e.target.value)}
                  options={courses.map((c) => {
                    const isLab = (c.courseType && String(c.courseType).toUpperCase().includes('LAB')) || (c.courseName && c.courseName.toUpperCase().includes('LAB'));
                    const typeLabel = isLab ? 'LABORATORY' : 'THEORY';
                    const sec = c.section || 'A';
                    return {
                      value: getCourseKey(c),
                      label: `${c.courseName} — Section ${sec} — ${typeLabel}`,
                    };
                  })}
                  placeholder={courses.length === 0 ? 'No courses assigned' : 'Select Course'}
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-theme-secondary mb-1.5">
                  Attendance Date <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  value={attendanceDate}
                  onChange={(e) => setAttendanceDate(e.target.value)}
                  className="block w-full rounded-lg border border-theme bg-theme-surface text-theme-primary text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-theme focus:border-theme-primary transition-all font-mono"
                />
              </div>

              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => loadRosterAndAttendance(selectedCourseKey, attendanceDate)}
                  icon={FiRefreshCw}
                  disabled={rosterLoading}
                  className="w-full justify-center"
                >
                  Reload Roster
                </Button>
              </div>
            </div>

            {/* Quick Summary Pill Bar */}
            {selectedCourseObj && (
              <div className="pt-3 border-t border-theme-subtle flex flex-wrap items-center justify-between gap-3 text-xs">
                <div className="flex items-center gap-4">
                  <span className="text-theme-secondary">
                    Total Enrolled: <strong className="text-theme-primary font-bold">{totalStudentsCount}</strong>
                  </span>
                  <span className="text-emerald-600 dark:text-emerald-400 font-semibold">
                    Present: <strong>{presentCount}</strong>
                  </span>
                  <span className="text-red-600 dark:text-red-400 font-semibold">
                    Absent: <strong>{absentCount}</strong>
                  </span>
                  <span className="px-2 py-0.5 rounded-full bg-theme-elevated text-theme-primary border border-theme font-bold">
                    Attendance Rate: {attendanceRate}%
                  </span>
                </div>

                {/* Bulk Marking Buttons */}
                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleMarkAll('PRESENT')}
                    icon={FiCheck}
                    disabled={rosterLoading || students.length === 0}
                    className="text-emerald-700 hover:bg-emerald-50 border-emerald-300"
                  >
                    Present All
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleMarkAll('ABSENT')}
                    icon={FiX}
                    disabled={rosterLoading || students.length === 0}
                    className="text-red-700 hover:bg-red-50 border-red-300"
                  >
                    Absent All
                  </Button>
                  <Button
                    size="sm"
                    variant="primary"
                    onClick={handleSaveAttendance}
                    icon={FiSave}
                    loading={saveLoading}
                    disabled={rosterLoading || students.length === 0}
                    className="font-bold shadow-sm"
                  >
                    Save Attendance
                  </Button>
                </div>
              </div>
            )}
          </div>

          {/* Feedback Alerts */}
          {saveSuccessMsg && (
            <div className="p-4 bg-emerald-50 border border-emerald-200 dark:bg-emerald-950/40 dark:border-emerald-800 rounded-xl flex items-center gap-3 text-sm text-emerald-800 dark:text-emerald-200 animate-in fade-in">
              <FiCheckCircle className="w-5 h-5 text-emerald-500 shrink-0" />
              <span className="font-semibold">{saveSuccessMsg}</span>
            </div>
          )}

          {saveErrorMsg && (
            <div className="p-4 bg-red-50 border border-red-200 dark:bg-red-950/40 dark:border-red-800 rounded-xl flex items-center gap-3 text-sm text-red-800 dark:text-red-200 animate-in fade-in">
              <FiAlertCircle className="w-5 h-5 text-red-500 shrink-0" />
              <span className="font-semibold">{saveErrorMsg}</span>
            </div>
          )}

          {rosterError && (
            <div className="p-4 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm font-semibold">
              {rosterError}
            </div>
          )}

          {/* Students Roster Table */}
          <div className="bg-theme-surface rounded-2xl border border-theme shadow-sm overflow-hidden">
            <div className="p-4 border-b border-theme flex items-center justify-between bg-theme-elevated/40">
              <div className="flex items-center gap-2 flex-wrap">
                <FiUsers className="text-theme-brand" />
                <h3 className="font-bold text-sm text-theme-primary">
                  Student Roster — {selectedCourseObj ? `${selectedCourseObj.courseCode} (${selectedCourseObj.courseName})` : 'Select Course'}
                </h3>
                {selectedCourseObj && (
                  <CourseTypeBadge type={selectedCourseObj.courseType} />
                )}
                {selectedCourseObj?.section && (
                  <SectionBadge section={selectedCourseObj.section} />
                )}
              </div>
              <span className="text-xs text-theme-secondary font-mono">Date: {attendanceDate}</span>
            </div>

            {rosterLoading ? (
              <div className="p-12 text-center text-theme-secondary text-sm">
                <div className="inline-block w-6 h-6 border-2 border-theme-brand border-t-transparent rounded-full animate-spin mb-2" />
                <p>Loading enrolled students and attendance status...</p>
              </div>
            ) : students.length === 0 ? (
              <div className="p-12 text-center text-theme-muted text-sm">
                No enrolled students found for this course section. Please select another course or contact administration.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-theme-elevated text-theme-secondary text-xs uppercase font-bold tracking-wider border-b border-theme">
                    <tr>
                      <th className="py-3 px-4 w-12 text-center">#</th>
                      <th className="py-3 px-4 w-36">Roll Number</th>
                      <th className="py-3 px-4">Student Name</th>
                      <th className="py-3 px-4 w-28 text-center">Program</th>
                      <th className="py-3 px-4 w-28 text-center">Section</th>
                      <th className="py-3 px-4 w-52 text-center">Attendance Status</th>
                      <th className="py-3 px-4">Remarks (Optional)</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-theme-subtle">
                    {students.map((st, idx) => {
                      const currentStatus = attendanceMap[st.studentId]?.status || 'PRESENT';
                      const currentRemarks = attendanceMap[st.studentId]?.remarks || '';

                      return (
                        <tr
                          key={st.studentId}
                          className={`hover:bg-theme-elevated/50 transition-colors ${
                            currentStatus === 'ABSENT' ? 'bg-red-50/30 dark:bg-red-950/10' : ''
                          }`}
                        >
                          <td className="py-3 px-4 text-center text-xs text-theme-muted font-mono">{idx + 1}</td>
                          <td className="py-3 px-4 font-mono font-bold text-theme-primary text-xs">
                            {st.studentRollNumber}
                          </td>
                          <td className="py-3 px-4 font-semibold text-theme-primary">
                            {st.studentName}
                          </td>
                          <td className="py-3 px-4 text-center">
                            <span className="font-semibold text-xs px-2 py-0.5 rounded bg-theme-elevated text-theme-primary border border-theme">
                              {st.programCode || st.programName || 'AIML'}
                            </span>
                          </td>
                          <td className="py-3 px-4 text-center">
                            <SectionBadge section={st.section} />
                          </td>
                          <td className="py-3 px-4 text-center">
                            <div className="inline-flex items-center gap-1.5 bg-theme-elevated p-1 rounded-lg border border-theme">
                              <button
                                type="button"
                                onClick={() => handleStudentStatusChange(st.studentId, 'PRESENT')}
                                className={`px-3 py-1 rounded text-xs font-bold transition-all ${
                                  currentStatus === 'PRESENT'
                                    ? 'bg-emerald-600 text-white shadow-sm'
                                    : 'text-theme-secondary hover:text-theme-primary hover:bg-theme-surface'
                                }`}
                              >
                                Present
                              </button>
                              <button
                                type="button"
                                onClick={() => handleStudentStatusChange(st.studentId, 'ABSENT')}
                                className={`px-3 py-1 rounded text-xs font-bold transition-all ${
                                  currentStatus === 'ABSENT'
                                    ? 'bg-red-600 text-white shadow-sm'
                                    : 'text-theme-secondary hover:text-theme-primary hover:bg-theme-surface'
                                }`}
                              >
                                Absent
                              </button>
                            </div>
                          </td>
                          <td className="py-3 px-4">
                            <input
                              type="text"
                              value={currentRemarks}
                              onChange={(e) => handleStudentRemarksChange(st.studentId, e.target.value)}
                              placeholder="e.g. Late, Field trip..."
                              className="w-full max-w-xs rounded border border-theme bg-theme-surface text-theme-primary text-xs px-2.5 py-1.5 focus:outline-none focus:ring-1 focus:ring-theme"
                            />
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}

            {/* Bottom Save Bar */}
            {students.length > 0 && (
              <div className="p-4 border-t border-theme bg-theme-elevated/40 flex items-center justify-between">
                <div className="text-xs text-theme-secondary">
                  Showing <strong>{students.length}</strong> students for {selectedCourseObj?.courseName} on {attendanceDate}
                </div>
                <Button
                  variant="primary"
                  size="md"
                  onClick={handleSaveAttendance}
                  icon={FiSave}
                  loading={saveLoading}
                  className="font-bold px-6 shadow-md"
                >
                  Save Attendance
                </Button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ================= TAB 2: ATTENDANCE HISTORY & LOGS ================= */}
      {activeTab === 'history' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between gap-4 bg-theme-surface p-4 rounded-xl border border-theme">
            <div className="w-80">
              <label className="block text-xs font-bold uppercase tracking-wider text-theme-secondary mb-1">
                Filter by Course & Section
              </label>
              <Select
                placeholder="All Assigned Courses & Sections"
                value={historyCourseKey}
                onChange={(e) => setHistoryCourseKey(e.target.value)}
                options={[
                  { value: '', label: 'All Assigned Courses & Sections' },
                  ...courses.map((c) => {
                    const isLab = (c.courseType && String(c.courseType).toUpperCase().includes('LAB')) || (c.courseName && c.courseName.toUpperCase().includes('LAB'));
                    const typeLabel = isLab ? 'LABORATORY' : 'THEORY';
                    const sec = c.section || 'A';
                    return {
                      value: getCourseKey(c),
                      label: `${c.courseName} — Section ${sec} — ${typeLabel}`,
                    };
                  }),
                ]}
              />
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => fetchHistory(historyPage, historyCourseKey)}
              icon={FiRefreshCw}
            >
              Refresh Logs
            </Button>
          </div>

          <Table
            columns={[
              { header: 'Date', accessor: 'attendanceDate', cellClassName: 'font-mono text-xs font-bold' },
              { header: 'Roll Number', accessor: 'studentRollNumber', cellClassName: 'font-mono text-xs font-semibold' },
              { header: 'Student Name', accessor: 'studentName' },
              {
                header: 'Course',
                render: (r) => (
                  <div>
                    <span className="font-semibold text-theme-primary block text-xs">{r.courseName || r.courseCode}</span>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <span className="font-mono text-[10px] text-theme-secondary">{r.courseCode}</span>
                      <CourseTypeBadge type={r.courseType} />
                    </div>
                  </div>
                ),
              },
              {
                header: 'Section',
                align: 'center',
                render: (r) => <SectionBadge section={r.section} />,
              },
              { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status} /> },
              { header: 'Remarks', accessor: 'remarks', render: (r) => r.remarks || '-' },
              {
                header: 'Actions',
                align: 'right',
                render: (r) => (
                  <div className="flex items-center justify-end gap-1.5">
                    <Button
                      size="sm"
                      variant="text"
                      onClick={() => handleOpenEdit(r)}
                      icon={FiEdit2}
                      aria-label="Edit attendance"
                    />
                    <Button
                      size="sm"
                      variant="text"
                      className="text-red-600 hover:text-red-700 hover:bg-red-50"
                      onClick={() => {
                        setDeletingId(r.attendanceId);
                        setIsDeleteDialogOpen(true);
                      }}
                      icon={FiTrash2}
                      aria-label="Delete attendance"
                    />
                  </div>
                ),
              },
            ]}
            data={records}
            loading={historyLoading}
            error={historyError}
            onRetry={() => fetchHistory(historyPage, historyCourseId)}
            emptyMessage="No attendance records recorded for the selected filter."
            keyField="attendanceId"
          />

          <Pagination
            currentPage={historyPage}
            totalPages={historyTotalPages}
            totalElements={historyTotalElements}
            pageSize={20}
            onPageChange={(newPage) => fetchHistory(newPage, historyCourseId)}
          />

          {/* Edit Attendance Modal */}
          <Modal
            isOpen={isEditModalOpen}
            onClose={() => setIsEditModalOpen(false)}
            title="Update Attendance Record"
          >
            <form onSubmit={handleEditSubmit} className="space-y-4">
              {editError && (
                <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
                  {editError}
                </div>
              )}

              <div className="bg-slate-50 dark:bg-slate-900 p-3 rounded-lg text-xs space-y-1 text-theme-secondary">
                <p><strong>Student:</strong> {editingRecord?.studentName} ({editingRecord?.studentRollNumber})</p>
                <p><strong>Course:</strong> {editingRecord?.courseName} ({editingRecord?.courseCode})</p>
                <p><strong>Date:</strong> {editingRecord?.attendanceDate}</p>
              </div>

              <Select
                label="Attendance Status"
                name="editStatus"
                required
                value={editStatus}
                onChange={(e) => setEditStatus(e.target.value)}
                options={[
                  { value: 'PRESENT', label: 'Present' },
                  { value: 'ABSENT', label: 'Absent' },
                  { value: 'LATE', label: 'Late' },
                  { value: 'EXCUSED', label: 'Excused' },
                ]}
              />

              <Input
                label="Remarks (Optional)"
                name="editRemarks"
                value={editRemarks}
                onChange={(e) => setEditRemarks(e.target.value)}
                placeholder="Update remarks..."
              />

              <div className="pt-4 flex justify-end gap-3 border-t border-theme-subtle">
                <Button variant="secondary" onClick={() => setIsEditModalOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" variant="primary" loading={editLoading}>
                  Update Record
                </Button>
              </div>
            </form>
          </Modal>

          {/* Delete Confirmation */}
          <ConfirmDialog
            isOpen={isDeleteDialogOpen}
            onClose={() => setIsDeleteDialogOpen(false)}
            onConfirm={handleDelete}
            title="Delete Attendance Record"
            message="Are you sure you want to permanently delete this attendance entry?"
            loading={deleteLoading}
          />
        </div>
      )}
    </div>
  );
};

export default FacultyAttendancePage;
