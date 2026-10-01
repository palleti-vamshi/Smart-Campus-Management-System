import React, { useState, useEffect } from 'react';
import { markService } from '../../services/markService';
import { examService } from '../../services/examService';
import { enrollmentService } from '../../services/enrollmentService';
import { courseService } from '../../services/courseService';
import { dashboardService } from '../../services/dashboardService';
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
  FiAward,
  FiList,
  FiSave,
  FiUsers,
  FiCheckCircle,
  FiAlertCircle,
  FiRefreshCw,
  FiEdit2,
  FiTrash2,
  FiPlus
} from 'react-icons/fi';

export const FacultyMarksPage = () => {
  const [activeTab, setActiveTab] = useState('entry'); // 'entry' | 'records'
  const [courses, setCourses] = useState([]);
  const [selectedCourseKey, setSelectedCourseKey] = useState('');
  const [exams, setExams] = useState([]);
  const [selectedExamId, setSelectedExamId] = useState('');

  const getCourseKey = (c) => `${c.courseId}___${c.section || 'A'}`;
  const parseCourseKey = (key) => {
    if (!key) return { courseId: null, section: null };
    const [cId, sec] = key.split('___');
    return { courseId: Number(cId), section: sec || null };
  };

  // Batch Marks Entry state
  const [students, setStudents] = useState([]);
  const [marksMap, setMarksMap] = useState({}); // { [studentId]: { marksObtained: '', remarks: '', grade: '' } }
  const [rosterLoading, setRosterLoading] = useState(false);
  const [rosterError, setRosterError] = useState(null);
  const [saveLoading, setSaveLoading] = useState(false);
  const [saveSuccessMsg, setSaveSuccessMsg] = useState('');
  const [saveErrorMsg, setSaveErrorMsg] = useState('');

  // Records / History state
  const [marks, setMarks] = useState([]);
  const [recordsExamId, setRecordsExamId] = useState('');
  const [recordsLoading, setRecordsLoading] = useState(false);
  const [recordsError, setRecordsError] = useState(null);
  const [recordsPage, setRecordsPage] = useState(0);
  const [recordsTotalPages, setRecordsTotalPages] = useState(1);
  const [recordsTotalElements, setRecordsTotalElements] = useState(0);

  // Single edit / create modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [modalExamId, setModalExamId] = useState('');
  const [modalStudentId, setModalStudentId] = useState('');
  const [modalMarksObtained, setModalMarksObtained] = useState('');
  const [modalRemarks, setModalRemarks] = useState('');
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete Dialog
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // 1. Fetch faculty courses & exams on mount
  useEffect(() => {
    courseService.getFacultyCourses()
      .then((res) => {
        const cList = res.data || [];
        setCourses(cList);
        if (cList.length > 0) {
          setSelectedCourseKey(getCourseKey(cList[0]));
        }
      })
      .catch(() => {
        dashboardService.getFacultyDashboard()
          .then((dRes) => {
            const cList = dRes.data?.assignedCourses || [];
            setCourses(cList);
            if (cList.length > 0) {
              setSelectedCourseKey(getCourseKey(cList[0]));
            }
          })
          .catch(() => {});
      });

    examService.getFacultyExams({ size: 100 })
      .then((res) => {
        const exList = res.data?.content || [];
        setExams(exList);
      })
      .catch(() => {});
  }, []);

  const selectedCourseObj = courses.find((c) => getCourseKey(c) === selectedCourseKey) || null;
  const { courseId: selectedCourseIdNum, section: selectedSection } = parseCourseKey(selectedCourseKey);

  // Filter exams for selected course
  const courseExams = exams.filter(
    (e) => !selectedCourseIdNum || String(e.courseId) === String(selectedCourseIdNum)
  );

  // Auto-select first exam when course changes or courseExams change
  useEffect(() => {
    if (courseExams.length > 0) {
      if (!selectedExamId || !courseExams.some((e) => String(e.examId) === String(selectedExamId))) {
        setSelectedExamId(String(courseExams[0].examId));
      }
    } else {
      setSelectedExamId('');
    }
  }, [selectedCourseKey, exams]);

  // Selected exam object
  const currentExamObj = exams.find((e) => String(e.examId) === String(selectedExamId));
  const maxMarksVal = currentExamObj?.maxMarks ? Number(currentExamObj.maxMarks) : 100;

  // Grade calculator helper
  const calculateGrade = (score, max) => {
    if (score === '' || score === null || score === undefined || isNaN(score) || max <= 0) return '-';
    const numScore = Number(score);
    const pct = (numScore / max) * 100;
    if (pct >= 90) return 'A+';
    if (pct >= 80) return 'A';
    if (pct >= 70) return 'B+';
    if (pct >= 60) return 'B';
    if (pct >= 50) return 'C';
    if (pct >= 40) return 'D';
    return 'F';
  };

  // 2. Load enrolled students and existing marks for selected Exam
  const loadRosterAndMarks = async (examId, courseKey) => {
    const { courseId, section } = parseCourseKey(courseKey);
    if (!examId || !courseId) {
      setStudents([]);
      setMarksMap({});
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

      // Fetch existing marks for this exam, course, and section
      let existingMarks = [];
      try {
        const markRes = await markService.getFacultyMarks({ examId, courseId, section, size: 200 });
        existingMarks = markRes.data?.content || [];
      } catch (e) {
        // non-fatal
      }

      // Populate map
      const initialMap = {};
      enrolled.forEach((st) => {
        const found = existingMarks.find(
          (m) => m.studentId === st.studentId || m.studentRollNumber === st.studentRollNumber
        );
        if (found) {
          initialMap[st.studentId] = {
            marksObtained: found.marksObtained !== null ? String(found.marksObtained) : '',
            remarks: found.remarks || '',
            grade: found.grade || '',
          };
        } else {
          initialMap[st.studentId] = {
            marksObtained: '',
            remarks: '',
            grade: '',
          };
        }
      });
      setMarksMap(initialMap);
    } catch (err) {
      setRosterError(err.response?.data?.message || err.message || 'Failed to load enrolled students.');
    } finally {
      setRosterLoading(false);
    }
  };

  useEffect(() => {
    if (selectedExamId && selectedCourseKey) {
      loadRosterAndMarks(selectedExamId, selectedCourseKey);
    }
  }, [selectedExamId, selectedCourseKey]);

  const handleScoreChange = (studentId, value) => {
    setMarksMap((prev) => ({
      ...prev,
      [studentId]: {
        ...prev[studentId],
        marksObtained: value,
        grade: calculateGrade(value, maxMarksVal),
      },
    }));
  };

  const handleRemarksChange = (studentId, value) => {
    setMarksMap((prev) => ({
      ...prev,
      [studentId]: {
        ...prev[studentId],
        remarks: value,
      },
    }));
  };

  // Submit batch marks
  const handleSaveMarks = async () => {
    if (!selectedExamId || students.length === 0) return;

    // Validate marks
    for (const st of students) {
      const entry = marksMap[st.studentId];
      if (entry?.marksObtained !== '' && entry?.marksObtained !== undefined) {
        const num = Number(entry.marksObtained);
        if (isNaN(num) || num < 0 || num > maxMarksVal) {
          setSaveErrorMsg(
            `Invalid mark for student ${st.studentRollNumber}: Score must be between 0 and ${maxMarksVal}.`
          );
          return;
        }
      }
    }

    setSaveLoading(true);
    setSaveSuccessMsg('');
    setSaveErrorMsg('');

    try {
      // Build items array (only save students that have a non-empty score)
      const itemsToSave = [];
      students.forEach((st) => {
        const entry = marksMap[st.studentId];
        if (entry?.marksObtained !== '' && entry?.marksObtained !== undefined) {
          itemsToSave.push({
            studentId: st.studentId,
            marksObtained: Number(entry.marksObtained),
            grade: calculateGrade(entry.marksObtained, maxMarksVal),
            remarks: entry.remarks?.trim() || null,
          });
        }
      });

      if (itemsToSave.length === 0) {
        setSaveErrorMsg('Please enter marks for at least one student before saving.');
        setSaveLoading(false);
        return;
      }

      await markService.recordBatchMarks({
        examId: Number(selectedExamId),
        items: itemsToSave,
      });

      setSaveSuccessMsg(
        `Marks for ${itemsToSave.length} students successfully saved for ${currentExamObj?.examName}!`
      );
      loadRosterAndMarks(selectedExamId, selectedCourseKey);
      if (activeTab === 'records') {
        fetchMarks(recordsPage, recordsExamId, recordsCourseKey);
      }
    } catch (err) {
      setSaveErrorMsg(err.response?.data?.message || err.message || 'Failed to save marks.');
    } finally {
      setSaveLoading(false);
    }
  };

  const [recordsCourseKey, setRecordsCourseKey] = useState('');

  // 3. Records / History Tab
  const fetchMarks = async (pageNumber = 0, examId = recordsExamId, courseKey = recordsCourseKey) => {
    setRecordsLoading(true);
    setRecordsError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (examId) params.examId = examId;
      const { courseId, section } = parseCourseKey(courseKey);
      if (courseId) params.courseId = courseId;
      if (section) params.section = section;
      const res = await markService.getFacultyMarks(params);
      const pageData = res.data;
      setMarks(pageData?.content || []);
      setRecordsPage(pageData?.pageNumber || 0);
      setRecordsTotalPages(pageData?.totalPages || 1);
      setRecordsTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setRecordsError(err.response?.data?.message || err.message || 'Failed to load marks.');
    } finally {
      setRecordsLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'records') {
      fetchMarks(0, recordsExamId, recordsCourseKey);
    }
  }, [activeTab, recordsExamId, recordsCourseKey]);

  const handleOpenEdit = (m) => {
    setIsEditing(true);
    setEditingId(m.markId);
    setModalExamId(String(m.examId));
    setModalMarksObtained(String(m.marksObtained));
    setModalRemarks(m.remarks || '');
    setFormError('');
    setIsModalOpen(true);
  };

  const handleModalSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      await markService.updateFacultyMark(editingId, {
        marksObtained: Number(modalMarksObtained),
        remarks: modalRemarks.trim() || undefined,
      });
      setIsModalOpen(false);
      fetchMarks(recordsPage, recordsExamId);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to update marks.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await markService.deleteFacultyMark(deletingId);
      setIsDeleteDialogOpen(false);
      fetchMarks(recordsPage, recordsExamId);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete mark.');
    } finally {
      setDeleteLoading(false);
    }
  };

  // Stats
  const filledCount = students.filter(
    (st) => marksMap[st.studentId]?.marksObtained !== '' && marksMap[st.studentId]?.marksObtained !== undefined
  ).length;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Marks & Grades Evaluation"
        subtitle="Enter student assessment scores by course roster or review evaluation history"
      >
        <div className="flex items-center gap-2 bg-theme-surface p-1 rounded-xl border border-theme">
          <button
            type="button"
            onClick={() => setActiveTab('entry')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'entry'
                ? 'bg-theme-brand text-white shadow-sm'
                : 'text-theme-secondary hover:text-theme-primary'
            }`}
          >
            <FiAward className="w-3.5 h-3.5" />
            Batch Marks Entry
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('records')}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'records'
                ? 'bg-theme-brand text-white shadow-sm'
                : 'text-theme-secondary hover:text-theme-primary'
            }`}
          >
            <FiList className="w-3.5 h-3.5" />
            Evaluation Records
          </button>
        </div>
      </PageHeader>

      {/* ================= TAB 1: BATCH MARKS ENTRY ================= */}
      {activeTab === 'entry' && (
        <div className="space-y-6">
          {/* Controls Bar */}
          <div className="bg-theme-surface p-5 rounded-2xl border border-theme shadow-sm space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-theme-secondary mb-1.5">
                  Assigned Course & Section <span className="text-red-500">*</span>
                </label>
                <Select
                  value={selectedCourseKey}
                  onChange={(e) => setSelectedCourseKey(e.target.value)}
                  options={courses.map((c) => {
                    const isLab = (c.courseType && String(c.courseType).toUpperCase().includes('LAB')) || (c.courseName && c.courseName.toUpperCase().includes('LAB'));
                    const typeLabel = isLab ? 'LABORATORY' : 'THEORY';
                    return {
                      value: getCourseKey(c),
                      label: `${c.courseName} [Section ${c.section || 'A'}] (${typeLabel})`,
                    };
                  })}
                  placeholder={courses.length === 0 ? 'No courses assigned' : 'Select Course'}
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-theme-secondary mb-1.5">
                  Examination / Assessment <span className="text-red-500">*</span>
                </label>
                <Select
                  value={selectedExamId}
                  onChange={(e) => setSelectedExamId(e.target.value)}
                  options={courseExams.map((ex) => ({
                    value: String(ex.examId),
                    label: `${ex.examName} (${ex.examType}) — Max: ${ex.maxMarks}`,
                  }))}
                  placeholder={courseExams.length === 0 ? 'No exams scheduled' : 'Select Examination'}
                />
              </div>

              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => loadRosterAndMarks(selectedExamId, selectedCourseKey)}
                  icon={FiRefreshCw}
                  disabled={rosterLoading || !selectedExamId}
                  className="w-full justify-center"
                >
                  Reload Scores
                </Button>
              </div>
            </div>

            {/* Assessment Meta & Save Bar */}
            {currentExamObj && (
              <div className="pt-3 border-t border-theme-subtle flex flex-wrap items-center justify-between gap-3 text-xs">
                <div className="flex items-center gap-4">
                  <span className="text-theme-secondary">
                    Total Students: <strong className="text-theme-primary font-bold">{students.length}</strong>
                  </span>
                  <span className="text-theme-secondary">
                    Scores Entered:{' '}
                    <strong className="text-theme-primary font-bold">
                      {filledCount} / {students.length}
                    </strong>
                  </span>
                  <span className="px-2.5 py-0.5 rounded-full bg-theme-elevated text-theme-primary border border-theme font-bold">
                    Max Score: {maxMarksVal}
                  </span>
                  {currentExamObj.weightage != null && currentExamObj.weightage !== '' && (
                    <span className="px-2.5 py-0.5 rounded-full bg-primary-50 dark:bg-primary-950/40 text-primary-700 dark:text-primary-300 border border-primary-200 dark:border-primary-800 font-bold">
                      Weightage: {currentExamObj.weightage}%
                    </span>
                  )}
                </div>

                <Button
                  size="sm"
                  variant="primary"
                  onClick={handleSaveMarks}
                  icon={FiSave}
                  loading={saveLoading}
                  disabled={rosterLoading || students.length === 0}
                  className="font-bold shadow-sm"
                >
                  Save All Marks
                </Button>
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

          {/* Marks Table */}
          <div className="bg-theme-surface rounded-2xl border border-theme shadow-sm overflow-hidden">
            <div className="p-4 border-b border-theme flex items-center justify-between bg-theme-elevated/40">
              <div className="flex items-center gap-2 flex-wrap">
                <FiUsers className="text-theme-brand" />
                <h3 className="font-bold text-sm text-theme-primary">
                  Student Marks Roster — {currentExamObj?.examName || 'Select Exam'}
                </h3>
                {selectedCourseObj && <CourseTypeBadge type={selectedCourseObj.courseType} />}
                {selectedCourseObj?.section && <SectionBadge section={selectedCourseObj.section} />}
              </div>
              <span className="text-xs text-theme-secondary font-mono">
                Date: {currentExamObj?.examDate || 'Scheduled'}
              </span>
            </div>

            {rosterLoading ? (
              <div className="p-12 text-center text-theme-secondary text-sm">
                <div className="inline-block w-6 h-6 border-2 border-theme-brand border-t-transparent rounded-full animate-spin mb-2" />
                <p>Loading students and recorded marks...</p>
              </div>
            ) : !selectedExamId ? (
              <div className="p-12 text-center text-theme-muted text-sm">
                Please select a course and examination from the dropdown above to load the student roster.
              </div>
            ) : students.length === 0 ? (
              <div className="p-12 text-center text-theme-muted text-sm">
                No enrolled students found for this course section.
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
                      <th className="py-3 px-4 w-40 text-center">Marks (Max: {maxMarksVal})</th>
                      <th className="py-3 px-4 w-24 text-center">Grade</th>
                      <th className="py-3 px-4">Remarks (Optional)</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-theme-subtle">
                    {students.map((st, idx) => {
                      const entry = marksMap[st.studentId] || { marksObtained: '', remarks: '', grade: '' };
                      const currentGrade = entry.grade || calculateGrade(entry.marksObtained, maxMarksVal);

                      return (
                        <tr key={st.studentId} className="hover:bg-theme-elevated/50 transition-colors">
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
                            <div className="flex items-center justify-center gap-1.5">
                              <input
                                type="number"
                                min="0"
                                max={maxMarksVal}
                                step="1"
                                value={entry.marksObtained}
                                onChange={(e) => handleScoreChange(st.studentId, e.target.value)}
                                placeholder="0"
                                className="w-24 text-center font-bold rounded-lg border border-theme bg-theme-surface text-theme-primary text-sm px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-theme focus:border-theme-primary transition-all font-mono"
                              />
                              <span className="text-xs text-theme-muted font-mono">/ {maxMarksVal}</span>
                            </div>
                          </td>
                          <td className="py-3 px-4 text-center">
                            <span
                              className={`inline-block px-2.5 py-0.5 rounded text-xs font-bold ${
                                currentGrade === 'F'
                                  ? 'bg-red-100 text-red-800 dark:bg-red-950/60 dark:text-red-300'
                                  : currentGrade.startsWith('A')
                                  ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300'
                                  : currentGrade !== '-'
                                  ? 'bg-sky-100 text-sky-800 dark:bg-sky-950/60 dark:text-sky-300'
                                  : 'bg-slate-100 text-slate-500 dark:bg-slate-800 dark:text-slate-400'
                              }`}
                            >
                              {currentGrade}
                            </span>
                          </td>
                          <td className="py-3 px-4">
                            <input
                              type="text"
                              value={entry.remarks}
                              onChange={(e) => handleRemarksChange(st.studentId, e.target.value)}
                              placeholder="e.g. Excellent, Re-test needed..."
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
                  Ready to commit scores for <strong>{filledCount}</strong> students in {currentExamObj?.examName}
                </div>
                <Button
                  variant="primary"
                  size="md"
                  onClick={handleSaveMarks}
                  icon={FiSave}
                  loading={saveLoading}
                  className="font-bold px-6 shadow-md"
                >
                  Save All Marks
                </Button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ================= TAB 2: EVALUATION RECORDS & HISTORY ================= */}
      {activeTab === 'records' && (
        <div className="space-y-6">
          <div className="flex flex-wrap items-center justify-between gap-4 bg-theme-surface p-4 rounded-xl border border-theme">
            <div className="flex flex-wrap items-center gap-4">
              <div className="w-72">
                <label className="block text-xs font-bold uppercase tracking-wider text-theme-secondary mb-1">
                  Filter by Course & Section
                </label>
                <Select
                  placeholder="All Assigned Courses & Sections"
                  value={recordsCourseKey}
                  onChange={(e) => setRecordsCourseKey(e.target.value)}
                  options={[
                    { value: '', label: 'All Assigned Courses & Sections' },
                    ...courses.map((c) => {
                      const isLab = (c.courseType && String(c.courseType).toUpperCase().includes('LAB')) || (c.courseName && c.courseName.toUpperCase().includes('LAB'));
                      const typeLabel = isLab ? 'LABORATORY' : 'THEORY';
                      return {
                        value: getCourseKey(c),
                        label: `${c.courseName} [Section ${c.section || 'A'}] (${typeLabel})`,
                      };
                    }),
                  ]}
                />
              </div>

              <div className="w-72">
                <label className="block text-xs font-bold uppercase tracking-wider text-theme-secondary mb-1">
                  Filter by Examination
                </label>
                <Select
                  placeholder="All Examinations"
                  value={recordsExamId}
                  onChange={(e) => setRecordsExamId(e.target.value)}
                  options={exams.map((ex) => ({
                    value: String(ex.examId),
                    label: `${ex.courseCode} - ${ex.examName} (${ex.examType})`,
                  }))}
                />
              </div>
            </div>

            <Button
              variant="outline"
              size="sm"
              onClick={() => fetchMarks(recordsPage, recordsExamId, recordsCourseKey)}
              icon={FiRefreshCw}
            >
              Refresh Records
            </Button>
          </div>

          <Table
            columns={[
              { header: 'Roll Number', accessor: 'studentRollNumber', cellClassName: 'font-mono text-xs font-bold' },
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
              { header: 'Exam', accessor: 'examName' },
              {
                header: 'Score',
                render: (r) => (
                  <span className="font-mono font-bold text-theme-primary">
                    {r.marksObtained} / {r.maxMarks}
                  </span>
                ),
                align: 'right',
              },
              {
                header: 'Grade',
                accessor: 'grade',
                align: 'center',
                render: (r) => (
                  <span className="inline-block px-2.5 py-0.5 rounded text-xs font-bold bg-theme-elevated text-theme-primary border border-theme">
                    {r.grade || '-'}
                  </span>
                ),
              },
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
                      aria-label="Edit marks"
                    />
                    <Button
                      size="sm"
                      variant="text"
                      className="text-red-600 hover:text-red-700 hover:bg-red-50"
                      onClick={() => {
                        setDeletingId(r.markId);
                        setIsDeleteDialogOpen(true);
                      }}
                      icon={FiTrash2}
                      aria-label="Delete marks"
                    />
                  </div>
                ),
              },
            ]}
            data={marks}
            loading={recordsLoading}
            error={recordsError}
            onRetry={() => fetchMarks(recordsPage, recordsExamId)}
            emptyMessage="No evaluation records found for the selected filter."
            keyField="markId"
          />

          <Pagination
            currentPage={recordsPage}
            totalPages={recordsTotalPages}
            totalElements={recordsTotalElements}
            pageSize={20}
            onPageChange={(newPage) => fetchMarks(newPage, recordsExamId)}
          />

          {/* Edit Modal */}
          <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            title="Update Evaluation Marks"
          >
            <form onSubmit={handleModalSubmit} className="space-y-4">
              {formError && (
                <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
                  {formError}
                </div>
              )}

              <Input
                label="Marks Obtained"
                name="marksObtained"
                type="number"
                step="1"
                min="0"
                required
                value={modalMarksObtained}
                onChange={(e) => setModalMarksObtained(e.target.value)}
                placeholder="Score achieved by student"
              />

              <Input
                label="Remarks (Optional)"
                name="remarks"
                value={modalRemarks}
                onChange={(e) => setModalRemarks(e.target.value)}
                placeholder="e.g. Excellent work, Re-test recommended"
              />

              <div className="pt-4 flex justify-end gap-3 border-t border-theme-subtle">
                <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" variant="primary" loading={formLoading}>
                  Update Score
                </Button>
              </div>
            </form>
          </Modal>

          {/* Delete Dialog */}
          <ConfirmDialog
            isOpen={isDeleteDialogOpen}
            onClose={() => setIsDeleteDialogOpen(false)}
            onConfirm={handleDelete}
            title="Delete Marks Entry"
            message="Are you sure you want to remove this mark record?"
            loading={deleteLoading}
          />
        </div>
      )}
    </div>
  );
};

export default FacultyMarksPage;
