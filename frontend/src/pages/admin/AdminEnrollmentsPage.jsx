import React, { useState, useEffect } from 'react';
import { enrollmentService } from '../../services/enrollmentService';
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

export const AdminEnrollmentsPage = () => {
  const [enrollments, setEnrollments] = useState([]);
  const [courses, setCourses] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filter
  const [selectedCourseId, setSelectedCourseId] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    studentId: '',
    courseId: '',
    academicYear: '2024-2025',
    semester: 1,
    status: 'ACTIVE',
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

  const fetchEnrollments = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedCourseId) params.courseId = selectedCourseId;
      if (selectedStatus) params.status = selectedStatus;
      const res = await enrollmentService.getAdminEnrollments(params);
      const pageData = res.data;
      setEnrollments(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load enrollments.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEnrollments(0);
  }, [selectedCourseId, selectedStatus]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      studentId: students[0]?.studentId ? String(students[0].studentId) : '',
      courseId: courses[0]?.courseId ? String(courses[0].courseId) : '',
      academicYear: '2024-2025',
      semester: 1,
      status: 'ACTIVE',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (e) => {
    setIsEditing(true);
    setEditingId(e.enrollmentId);
    setFormData({
      studentId: String(e.studentId),
      courseId: String(e.courseId),
      academicYear: e.academicYear || '2024-2025',
      semester: e.semester || 1,
      status: e.status || 'ACTIVE',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      const payload = {
        studentId: Number(formData.studentId),
        courseId: Number(formData.courseId),
        academicYear: formData.academicYear,
        semester: Number(formData.semester),
        status: formData.status,
      };

      if (isEditing) {
        await enrollmentService.updateEnrollment(editingId, payload);
      } else {
        await enrollmentService.createEnrollment(payload);
      }
      setIsModalOpen(false);
      fetchEnrollments(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save enrollment.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await enrollmentService.deleteEnrollment(deletingId);
      setIsDeleteDialogOpen(false);
      fetchEnrollments(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete enrollment.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Student Roll No', accessor: 'studentRollNumber', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Student Name', accessor: 'studentName' },
    { header: 'Course Code', accessor: 'courseCode' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Academic Year', accessor: 'academicYear' },
    { header: 'Semester', accessor: 'semester', align: 'center' },
    { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status} /> },
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
            aria-label="Edit enrollment"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.enrollmentId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete enrollment"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Student Enrollments"
        subtitle="Manage course registrations, academic year batches, and student statuses"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Enroll Student
        </Button>
      </PageHeader>

      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="w-full sm:w-64">
          <Select
            placeholder="All Courses"
            value={selectedCourseId}
            onChange={(e) => setSelectedCourseId(e.target.value)}
            options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
          />
        </div>
        <div className="w-full sm:w-48">
          <Select
            placeholder="All Statuses"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            options={[
              { value: 'ACTIVE', label: 'Active' },
              { value: 'COMPLETED', label: 'Completed' },
              { value: 'DROPPED', label: 'Dropped' },
            ]}
          />
        </div>
      </div>

      <Table
        columns={columns}
        data={enrollments}
        loading={loading}
        error={error}
        onRetry={() => fetchEnrollments(page)}
        emptyMessage="No enrollments found for current filters."
        keyField="enrollmentId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchEnrollments(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Edit Enrollment' : 'Enroll Student in Course'}
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
                label="Student"
                name="studentId"
                required
                value={formData.studentId}
                onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
                options={students.map((s) => ({ value: String(s.studentId), label: `${s.rollNumber} - ${s.firstName} ${s.lastName || ''}` }))}
              />
              <Select
                label="Course"
                name="courseId"
                required
                value={formData.courseId}
                onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
                options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
              />
            </>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Academic Year"
              name="academicYear"
              required
              value={formData.academicYear}
              onChange={(e) => setFormData({ ...formData, academicYear: e.target.value })}
              placeholder="e.g. 2024-2025"
            />
            <Select
              label="Semester"
              name="semester"
              required
              value={String(formData.semester)}
              onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
              options={[1, 2, 3, 4, 5, 6, 7, 8].map((s) => ({ value: String(s), label: `Semester ${s}` }))}
            />
          </div>

          <Select
            label="Enrollment Status"
            name="status"
            required
            value={formData.status}
            onChange={(e) => setFormData({ ...formData, status: e.target.value })}
            options={[
              { value: 'ACTIVE', label: 'Active' },
              { value: 'COMPLETED', label: 'Completed' },
              { value: 'DROPPED', label: 'Dropped' },
            ]}
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Confirm Enrollment'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Enrollment"
        message="Are you sure you want to delete this course enrollment? Student attendance and examination eligibility will be removed."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminEnrollmentsPage;
