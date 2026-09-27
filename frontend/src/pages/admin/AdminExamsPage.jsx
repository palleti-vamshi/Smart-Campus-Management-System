import React, { useState, useEffect } from 'react';
import { examService } from '../../services/examService';
import { courseService } from '../../services/courseService';
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

export const AdminExamsPage = () => {
  const [exams, setExams] = useState([]);
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [selectedCourseId, setSelectedCourseId] = useState('');
  const [selectedType, setSelectedType] = useState('');

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    courseId: '',
    examName: '',
    examType: 'MID_TERM',
    examDate: new Date().toISOString().split('T')[0],
    startTime: '09:00',
    endTime: '12:00',
    maxMarks: 100,
    weightage: 30,
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    courseService.getCourses({ size: 100 })
      .then((res) => setCourses(res.data?.content || []))
      .catch(() => {});
  }, []);

  const fetchExams = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedCourseId) params.courseId = selectedCourseId;
      if (selectedType) params.examType = selectedType;
      const res = await examService.getAdminExams(params);
      const pageData = res.data;
      setExams(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load exams.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExams(0);
  }, [selectedCourseId, selectedType]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      courseId: courses[0]?.courseId ? String(courses[0].courseId) : '',
      examName: '',
      examType: 'MID_TERM',
      examDate: new Date().toISOString().split('T')[0],
      startTime: '09:00',
      endTime: '12:00',
      maxMarks: 100,
      weightage: 30,
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (exam) => {
    setIsEditing(true);
    setEditingId(exam.examId);
    setFormData({
      courseId: String(exam.courseId),
      examName: exam.examName,
      examType: exam.examType,
      examDate: exam.examDate,
      startTime: exam.startTime || '09:00',
      endTime: exam.endTime || '12:00',
      maxMarks: exam.maxMarks,
      weightage: exam.weightage,
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
        ...formData,
        courseId: Number(formData.courseId),
        maxMarks: Number(formData.maxMarks),
        weightage: Number(formData.weightage),
      };

      if (isEditing) {
        await examService.updateAdminExam(editingId, payload);
      } else {
        await examService.createAdminExam(payload);
      }
      setIsModalOpen(false);
      fetchExams(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save exam.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await examService.deleteAdminExam(deletingId);
      setIsDeleteDialogOpen(false);
      fetchExams(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete exam.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Course', accessor: 'courseName', render: (r) => `${r.courseName} (${r.courseCode})` },
    { header: 'Exam Name', accessor: 'examName' },
    { header: 'Type', accessor: 'examType', render: (r) => <StatusBadge status={r.examType} /> },
    { header: 'Date', accessor: 'examDate' },
    { header: 'Time Slot', render: (r) => `${r.startTime || ''} - ${r.endTime || ''}` },
    { header: 'Max Marks', accessor: 'maxMarks', align: 'right' },
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
            aria-label="Edit exam"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.examId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete exam"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Examination Administration"
        subtitle="Schedule and configure department exams, dates, timeslots, and weightages"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Create Exam
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
            placeholder="All Exam Types"
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            options={[
              { value: 'QUIZ', label: 'Quiz' },
              { value: 'ASSIGNMENT', label: 'Assignment' },
              { value: 'MID_TERM', label: 'Mid-Term Exam' },
              { value: 'END_TERM', label: 'End-Term Exam' },
              { value: 'LAB', label: 'Lab Assessment' },
              { value: 'PROJECT', label: 'Project' },
            ]}
          />
        </div>
      </div>

      <Table
        columns={columns}
        data={exams}
        loading={loading}
        error={error}
        onRetry={() => fetchExams(page)}
        emptyMessage="No exams found matching criteria."
        keyField="examId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchExams(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Modify Examination' : 'Schedule Department Exam'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <Select
            label="Course"
            name="courseId"
            required
            value={formData.courseId}
            onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
            options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
          />

          <Input
            label="Exam Name"
            name="examName"
            required
            value={formData.examName}
            onChange={(e) => setFormData({ ...formData, examName: e.target.value })}
            placeholder="e.g. End Semester Theory Examination"
          />

          <Select
            label="Exam Type"
            name="examType"
            required
            value={formData.examType}
            onChange={(e) => setFormData({ ...formData, examType: e.target.value })}
            options={[
              { value: 'QUIZ', label: 'Quiz' },
              { value: 'ASSIGNMENT', label: 'Assignment' },
              { value: 'MID_TERM', label: 'Mid-Term Exam' },
              { value: 'END_TERM', label: 'End-Term Exam' },
              { value: 'LAB', label: 'Lab Assessment' },
              { value: 'PROJECT', label: 'Project' },
            ]}
          />

          <Input
            label="Exam Date"
            name="examDate"
            type="date"
            required
            value={formData.examDate}
            onChange={(e) => setFormData({ ...formData, examDate: e.target.value })}
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Start Time"
              name="startTime"
              type="time"
              required
              value={formData.startTime}
              onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
            />
            <Input
              label="End Time"
              name="endTime"
              type="time"
              required
              value={formData.endTime}
              onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Max Marks"
              name="maxMarks"
              type="number"
              min="1"
              required
              value={formData.maxMarks}
              onChange={(e) => setFormData({ ...formData, maxMarks: e.target.value })}
            />
            <Input
              label="Weightage (%)"
              name="weightage"
              type="number"
              min="1"
              max="100"
              required
              value={formData.weightage}
              onChange={(e) => setFormData({ ...formData, weightage: e.target.value })}
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Schedule Exam'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Examination"
        message="Are you sure you want to permanently delete this examination? Recorded student marks will be lost."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminExamsPage;
