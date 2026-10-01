import React, { useState, useEffect } from 'react';
import { examService } from '../../services/examService';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import StatusBadge, { CourseTypeBadge } from '../../components/common/Badge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

export const FacultyExamsPage = () => {
  const [exams, setExams] = useState([]);
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [courseId, setCourseId] = useState('');
  const [examName, setExamName] = useState('');
  const [examType, setExamType] = useState('MID_TERM');
  const [examDate, setExamDate] = useState('');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('12:00');
  const [maxMarks, setMaxMarks] = useState(100);
  const [weightage, setWeightage] = useState(30);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    dashboardService.getFacultyDashboard()
      .then((res) => {
        setCourses(res.data?.assignedCourses || []);
      })
      .catch(() => {});
  }, []);

  const fetchExams = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const res = await examService.getFacultyExams({ page: pageNumber, size: 20 });
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
  }, []);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setCourseId(courses[0]?.courseId ? String(courses[0].courseId) : '');
    setExamName('');
    setExamType('MID_TERM');
    setExamDate(new Date().toISOString().split('T')[0]);
    setStartTime('09:00');
    setEndTime('12:00');
    setMaxMarks(100);
    setWeightage(30);
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (exam) => {
    setIsEditing(true);
    setEditingId(exam.examId);
    setCourseId(String(exam.courseId));
    setExamName(exam.examName);
    setExamType(exam.examType);
    setExamDate(exam.examDate);
    setStartTime(exam.startTime || '09:00');
    setEndTime(exam.endTime || '12:00');
    setMaxMarks(exam.maxMarks);
    setWeightage(exam.weightage);
    setFormError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      const payload = {
        courseId: Number(courseId),
        examName: examName.trim(),
        examType,
        examDate,
        startTime,
        endTime,
        maxMarks: Number(maxMarks),
        weightage: Number(weightage),
      };

      if (isEditing) {
        await examService.updateFacultyExam(editingId, payload);
      } else {
        await examService.createFacultyExam(payload);
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
      await examService.deleteFacultyExam(deletingId);
      setIsDeleteDialogOpen(false);
      fetchExams(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete exam.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    const parts = String(dateStr).split('-');
    if (parts.length === 3) {
      return `${parts[2]}-${parts[1]}-${parts[0]}`;
    }
    return dateStr;
  };

  const columns = [
    { header: 'Course', accessor: 'courseName', render: (r) => `${r.courseName} (${r.courseCode})` },
    {
      header: 'Type',
      accessor: 'courseType',
      render: (r) => {
        const cType = r.courseType || (r.courseName && r.courseName.toUpperCase().includes('LAB') ? 'LABORATORY' : 'THEORY');
        return <CourseTypeBadge type={cType} />;
      },
    },
    { header: 'Exam Name', accessor: 'examName' },
    { header: 'Exam Type', accessor: 'examType', render: (r) => <StatusBadge status={r.examType} /> },
    { header: 'Date', accessor: 'examDate', render: (r) => <span className="font-mono text-xs font-semibold">{formatDate(r.examDate)}</span> },
    { header: 'Time Slot', render: (r) => `${r.startTime || ''} - ${r.endTime || ''}` },
    { header: 'Max Marks', accessor: 'maxMarks', align: 'right', render: (r) => <span className="font-mono font-bold text-theme-primary">{r.maxMarks}</span> },
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
        title="Course Examinations"
        subtitle="Schedule and manage exams for your assigned courses"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Schedule Exam
        </Button>
      </PageHeader>

      <Table
        columns={columns}
        data={exams}
        loading={loading}
        error={error}
        onRetry={() => fetchExams(page)}
        emptyMessage="No exams scheduled yet."
        keyField="examId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchExams(newPage)}
      />

      {/* Create / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Edit Examination' : 'Schedule New Examination'}
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
            value={courseId}
            onChange={(e) => setCourseId(e.target.value)}
            options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
          />

          <Input
            label="Exam Name"
            name="examName"
            required
            value={examName}
            onChange={(e) => setExamName(e.target.value)}
            placeholder="e.g. Mid-Term Assessment 1"
          />

          <Select
            label="Exam Type"
            name="examType"
            required
            value={examType}
            onChange={(e) => setExamType(e.target.value)}
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
            value={examDate}
            onChange={(e) => setExamDate(e.target.value)}
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Start Time"
              name="startTime"
              type="time"
              required
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
            />
            <Input
              label="End Time"
              name="endTime"
              type="time"
              required
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Max Marks"
              name="maxMarks"
              type="number"
              min="1"
              required
              value={maxMarks}
              onChange={(e) => setMaxMarks(e.target.value)}
            />
            <Input
              label="Weightage (%)"
              name="weightage"
              type="number"
              min="1"
              max="100"
              required
              value={weightage}
              onChange={(e) => setWeightage(e.target.value)}
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Update Exam' : 'Schedule Exam'}
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
        message="Are you sure you want to delete this examination? Associated marks will also be impacted."
        loading={deleteLoading}
      />
    </div>
  );
};

export default FacultyExamsPage;
