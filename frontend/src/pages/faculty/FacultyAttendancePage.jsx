import React, { useState, useEffect } from 'react';
import { attendanceService } from '../../services/attendanceService';
import { enrollmentService } from '../../services/enrollmentService';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/Badge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

export const FacultyAttendancePage = () => {
  const [records, setRecords] = useState([]);
  const [courses, setCourses] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState('');
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Mark Modal
  const [isMarkModalOpen, setIsMarkModalOpen] = useState(false);
  const [markCourseId, setMarkCourseId] = useState('');
  const [markStudentId, setMarkStudentId] = useState('');
  const [markDate, setMarkDate] = useState(new Date().toISOString().split('T')[0]);
  const [markStatus, setMarkStatus] = useState('PRESENT');
  const [markRemarks, setMarkRemarks] = useState('');
  const [markLoading, setMarkLoading] = useState(false);
  const [markError, setMarkError] = useState('');

  // Edit Modal
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [editStatus, setEditStatus] = useState('PRESENT');
  const [editRemarks, setEditRemarks] = useState('');
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState('');

  // Delete Dialog
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Load faculty courses
  useEffect(() => {
    dashboardService.getFacultyDashboard()
      .then((res) => setCourses(res.data?.assignedCourses || []))
      .catch(() => {});
  }, []);

  // When markCourseId changes in Mark Modal, fetch enrolled students for that course
  useEffect(() => {
    if (!markCourseId) {
      setStudents([]);
      return;
    }
    enrollmentService.getFacultyEnrollments({ courseId: markCourseId, size: 100 })
      .then((res) => {
        setStudents(res.data?.content || []);
        if (res.data?.content?.length > 0) {
          setMarkStudentId(String(res.data.content[0].studentId));
        }
      })
      .catch(() => setStudents([]));
  }, [markCourseId]);

  const fetchAttendance = async (pageNumber = 0, courseId = selectedCourseId) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (courseId) params.courseId = courseId;
      const res = await attendanceService.getFacultyAttendance(params);
      const pageData = res.data;
      setRecords(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load attendance.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAttendance(0, selectedCourseId);
  }, [selectedCourseId]);

  const handleOpenMarkModal = () => {
    const initialCourse = courses[0]?.courseId ? String(courses[0].courseId) : '';
    setMarkCourseId(initialCourse);
    setMarkDate(new Date().toISOString().split('T')[0]);
    setMarkStatus('PRESENT');
    setMarkRemarks('');
    setMarkError('');
    setIsMarkModalOpen(true);
  };

  const handleMarkSubmit = async (e) => {
    e.preventDefault();
    if (!markCourseId || !markStudentId || !markDate) {
      setMarkError('Course, student, and date are required.');
      return;
    }

    setMarkLoading(true);
    setMarkError('');
    try {
      await attendanceService.recordFacultyAttendance({
        courseId: Number(markCourseId),
        studentId: Number(markStudentId),
        attendanceDate: markDate,
        status: markStatus,
        remarks: markRemarks.trim() || undefined,
      });
      setIsMarkModalOpen(false);
      fetchAttendance(page);
    } catch (err) {
      setMarkError(err.response?.data?.message || err.message || 'Failed to record attendance.');
    } finally {
      setMarkLoading(false);
    }
  };

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
      fetchAttendance(page);
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
      fetchAttendance(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete attendance record.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Date', accessor: 'attendanceDate', cellClassName: 'font-mono text-xs' },
    { header: 'Roll Number', accessor: 'studentRollNumber', cellClassName: 'font-semibold' },
    { header: 'Student Name', accessor: 'studentName' },
    { header: 'Course Code', accessor: 'courseCode' },
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
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Attendance Management"
        subtitle="Record daily course attendance, update absence logs, and manage records"
      >
        <div className="flex items-center gap-3">
          <div className="w-56">
            <Select
              placeholder="All Assigned Courses"
              value={selectedCourseId}
              onChange={(e) => setSelectedCourseId(e.target.value)}
              options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode}` }))}
            />
          </div>
          <Button
            variant="primary"
            onClick={handleOpenMarkModal}
            icon={FiPlus}
          >
            Mark Attendance
          </Button>
        </div>
      </PageHeader>

      <Table
        columns={columns}
        data={records}
        loading={loading}
        error={error}
        onRetry={() => fetchAttendance(page)}
        emptyMessage="No attendance records recorded for the selected filter."
        keyField="attendanceId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchAttendance(newPage)}
      />

      {/* Mark Attendance Modal */}
      <Modal
        isOpen={isMarkModalOpen}
        onClose={() => setIsMarkModalOpen(false)}
        title="Record Student Attendance"
      >
        <form onSubmit={handleMarkSubmit} className="space-y-4">
          {markError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {markError}
            </div>
          )}

          <Select
            label="Assigned Course"
            name="markCourse"
            required
            value={markCourseId}
            onChange={(e) => setMarkCourseId(e.target.value)}
            options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
          />

          <Select
            label="Enrolled Student"
            name="markStudent"
            required
            value={markStudentId}
            onChange={(e) => setMarkStudentId(e.target.value)}
            options={students.map((s) => ({ value: String(s.studentId), label: `${s.studentRollNumber} - ${s.studentName}` }))}
            placeholder={students.length === 0 ? 'No students enrolled' : 'Select Student'}
          />

          <Input
            label="Attendance Date"
            name="markDate"
            type="date"
            required
            value={markDate}
            onChange={(e) => setMarkDate(e.target.value)}
          />

          <Select
            label="Attendance Status"
            name="markStatus"
            required
            value={markStatus}
            onChange={(e) => setMarkStatus(e.target.value)}
            options={[
              { value: 'PRESENT', label: 'Present' },
              { value: 'ABSENT', label: 'Absent' },
              { value: 'LATE', label: 'Late' },
              { value: 'EXCUSED', label: 'Excused' },
            ]}
          />

          <Input
            label="Remarks (Optional)"
            name="markRemarks"
            value={markRemarks}
            onChange={(e) => setMarkRemarks(e.target.value)}
            placeholder="e.g. Medical excuse, Field visit"
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsMarkModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={markLoading}>
              Save Attendance
            </Button>
          </div>
        </form>
      </Modal>

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

          <div className="bg-slate-50 p-3 rounded-lg text-xs space-y-1 text-slate-600">
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

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={editLoading}>
              Update
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
  );
};

export default FacultyAttendancePage;
