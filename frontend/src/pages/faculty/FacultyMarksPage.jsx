import React, { useState, useEffect } from 'react';
import { markService } from '../../services/markService';
import { examService } from '../../services/examService';
import { enrollmentService } from '../../services/enrollmentService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

export const FacultyMarksPage = () => {
  const [marks, setMarks] = useState([]);
  const [exams, setExams] = useState([]);
  const [students, setStudents] = useState([]);
  const [selectedExamId, setSelectedExamId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Enter Marks Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [modalExamId, setModalExamId] = useState('');
  const [modalStudentId, setModalStudentId] = useState('');
  const [marksObtained, setMarksObtained] = useState('');
  const [remarks, setRemarks] = useState('');
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Fetch faculty exams
  useEffect(() => {
    examService.getFacultyExams({ size: 100 })
      .then((res) => {
        setExams(res.data?.content || []);
      })
      .catch(() => {});
  }, []);

  // When modalExamId changes in Create modal, fetch enrolled students for that exam's course
  useEffect(() => {
    if (!modalExamId) {
      setStudents([]);
      return;
    }
    const examObj = exams.find((e) => String(e.examId) === String(modalExamId));
    if (examObj?.courseId) {
      enrollmentService.getFacultyEnrollments({ courseId: examObj.courseId, size: 100 })
        .then((res) => {
          setStudents(res.data?.content || []);
          if (res.data?.content?.length > 0) {
            setModalStudentId(String(res.data.content[0].studentId));
          }
        })
        .catch(() => setStudents([]));
    }
  }, [modalExamId, exams]);

  const fetchMarks = async (pageNumber = 0, examId = selectedExamId) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (examId) params.examId = examId;
      const res = await markService.getFacultyMarks(params);
      const pageData = res.data;
      setMarks(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load marks.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMarks(0, selectedExamId);
  }, [selectedExamId]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    const firstExamId = exams[0]?.examId ? String(exams[0].examId) : '';
    setModalExamId(firstExamId);
    setMarksObtained('');
    setRemarks('');
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (m) => {
    setIsEditing(true);
    setEditingId(m.markId);
    setModalExamId(String(m.examId));
    setMarksObtained(String(m.marksObtained));
    setRemarks(m.remarks || '');
    setFormError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      if (isEditing) {
        await markService.updateFacultyMark(editingId, {
          marksObtained: Number(marksObtained),
          remarks: remarks.trim() || undefined,
        });
      } else {
        await markService.createFacultyMark({
          examId: Number(modalExamId),
          studentId: Number(modalStudentId),
          marksObtained: Number(marksObtained),
          remarks: remarks.trim() || undefined,
        });
      }
      setIsModalOpen(false);
      fetchMarks(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save marks.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await markService.deleteFacultyMark(deletingId);
      setIsDeleteDialogOpen(false);
      fetchMarks(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete mark.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Roll Number', accessor: 'studentRollNumber', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Student Name', accessor: 'studentName' },
    { header: 'Course', accessor: 'courseName' },
    { header: 'Exam', accessor: 'examName' },
    { header: 'Score', render: (r) => `${r.marksObtained} / ${r.maxMarks}`, align: 'right' },
    {
      header: 'Grade',
      accessor: 'grade',
      align: 'center',
      render: (r) => (
        <span className="inline-block px-2 py-0.5 rounded text-xs font-bold bg-slate-100 text-slate-800">
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
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Marks & Grades Evaluation"
        subtitle="Enter student assessment scores and review calculated grades"
      >
        <div className="flex items-center gap-3">
          <div className="w-56">
            <Select
              placeholder="All Exams"
              value={selectedExamId}
              onChange={(e) => setSelectedExamId(e.target.value)}
              options={exams.map((ex) => ({ value: String(ex.examId), label: `${ex.courseCode} - ${ex.examName}` }))}
            />
          </div>
          <Button
            variant="primary"
            onClick={handleOpenCreate}
            icon={FiPlus}
          >
            Enter Marks
          </Button>
        </div>
      </PageHeader>

      <Table
        columns={columns}
        data={marks}
        loading={loading}
        error={error}
        onRetry={() => fetchMarks(page)}
        emptyMessage="No evaluation records found for the selected filter."
        keyField="markId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchMarks(newPage)}
      />

      {/* Enter / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Update Evaluation Marks' : 'Enter Assessment Score'}
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
                label="Examination"
                name="modalExam"
                required
                value={modalExamId}
                onChange={(e) => setModalExamId(e.target.value)}
                options={exams.map((ex) => ({ value: String(ex.examId), label: `${ex.courseCode} - ${ex.examName} (Max: ${ex.maxMarks})` }))}
              />

              <Select
                label="Student"
                name="modalStudent"
                required
                value={modalStudentId}
                onChange={(e) => setModalStudentId(e.target.value)}
                options={students.map((s) => ({ value: String(s.studentId), label: `${s.studentRollNumber} - ${s.studentName}` }))}
                placeholder={students.length === 0 ? 'No enrolled students' : 'Select Student'}
              />
            </>
          )}

          <Input
            label="Marks Obtained"
            name="marksObtained"
            type="number"
            step="0.5"
            min="0"
            required
            value={marksObtained}
            onChange={(e) => setMarksObtained(e.target.value)}
            placeholder="Score achieved by student"
          />

          <Input
            label="Remarks (Optional)"
            name="remarks"
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="e.g. Excellent work, Re-test recommended"
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Update Score' : 'Save Marks'}
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
  );
};

export default FacultyMarksPage;
