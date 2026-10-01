import React, { useState, useEffect } from 'react';
import { attendanceService } from '../../services/attendanceService';
import { courseService } from '../../services/courseService';
import { studentService } from '../../services/studentService';
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

export const AdminAttendancePage = () => {
  const [records, setRecords] = useState([]);
  const [courses, setCourses] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [selectedCourseId, setSelectedCourseId] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedDate, setSelectedDate] = useState('');

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    courseId: '',
    studentId: '',
    attendanceDate: new Date().toISOString().split('T')[0],
    status: 'PRESENT',
    remarks: '',
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    Promise.all([
      courseService.getCourses({ size: 100 }),
      studentService.getStudents({ size: 500 }),
    ]).then(([cRes, sRes]) => {
      setCourses(cRes?.data?.content || cRes?.data || []);
      setStudents(sRes?.data?.content || sRes?.data || []);
    }).catch(() => {});
  }, []);

  const fetchAttendance = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedCourseId) params.courseId = selectedCourseId;
      if (selectedStatus) params.status = selectedStatus;
      if (selectedDate) params.attendanceDate = selectedDate;
      const res = await attendanceService.getAdminAttendance(params);
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
    fetchAttendance(0);
  }, [selectedCourseId, selectedStatus, selectedDate]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      courseId: courses[0]?.courseId ? String(courses[0].courseId) : '',
      studentId: students[0]?.studentId ? String(students[0].studentId) : '',
      attendanceDate: new Date().toISOString().split('T')[0],
      status: 'PRESENT',
      remarks: '',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (rec) => {
    setIsEditing(true);
    setEditingId(rec.attendanceId);
    setFormData({
      courseId: String(rec.courseId),
      studentId: String(rec.studentId),
      attendanceDate: rec.attendanceDate,
      status: rec.status,
      remarks: rec.remarks || '',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      if (isEditing) {
        await attendanceService.updateAdminAttendance(editingId, {
          status: formData.status,
          remarks: formData.remarks.trim() || undefined,
        });
      } else {
        await attendanceService.recordAdminAttendance({
          courseId: Number(formData.courseId),
          studentId: Number(formData.studentId),
          attendanceDate: formData.attendanceDate,
          status: formData.status,
          remarks: formData.remarks.trim() || undefined,
        });
      }
      setIsModalOpen(false);
      fetchAttendance(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save attendance record.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await attendanceService.deleteAdminAttendance(deletingId);
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
    { header: 'Roll No', accessor: 'studentRollNumber', cellClassName: 'font-semibold' },
    { header: 'Student Name', accessor: 'studentName' },
    { header: 'Course', accessor: 'courseName', render: (r) => `${r.courseName} (${r.courseCode})` },
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
        title="Department Attendance Logs"
        subtitle="Administrative oversight and override controls for student attendance"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Add Record
        </Button>
      </PageHeader>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <Select
          placeholder="All Courses"
          value={selectedCourseId}
          onChange={(e) => setSelectedCourseId(e.target.value)}
          options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
        />
        <Select
          placeholder="All Statuses"
          value={selectedStatus}
          onChange={(e) => setSelectedStatus(e.target.value)}
          options={[
            { value: 'PRESENT', label: 'Present' },
            { value: 'ABSENT', label: 'Absent' },
            { value: 'LATE', label: 'Late' },
            { value: 'EXCUSED', label: 'Excused' },
          ]}
        />
        <Input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
        />
      </div>

      <Table
        columns={columns}
        data={records}
        loading={loading}
        error={error}
        onRetry={() => fetchAttendance(page)}
        emptyMessage="No attendance records found matching filters."
        keyField="attendanceId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchAttendance(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Modify Attendance Record' : 'Record Student Attendance'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          {!isEditing && (
            <>
              <Select
                label="Course"
                name="courseId"
                required
                value={formData.courseId}
                onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
                options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
              />
              <Select
                label="Student"
                name="studentId"
                required
                value={formData.studentId}
                onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
                options={students.map((s) => ({ value: String(s.studentId), label: `${s.rollNumber} - ${s.firstName} ${s.lastName || ''}` }))}
              />
            </>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Attendance Date"
              name="attendanceDate"
              type="date"
              required
              disabled={isEditing}
              value={formData.attendanceDate}
              onChange={(e) => setFormData({ ...formData, attendanceDate: e.target.value })}
            />
            <Select
              label="Status"
              name="status"
              required
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              options={[
                { value: 'PRESENT', label: 'Present' },
                { value: 'ABSENT', label: 'Absent' },
                { value: 'LATE', label: 'Late' },
                { value: 'EXCUSED', label: 'Excused' },
              ]}
            />
          </div>

          <Input
            label="Remarks (Optional)"
            name="remarks"
            value={formData.remarks}
            onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
            placeholder="Administrative note..."
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Record Attendance'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Attendance Record"
        message="Are you sure you want to permanently delete this attendance record?"
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminAttendancePage;
