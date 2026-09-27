import React, { useState, useEffect } from 'react';
import { markService } from '../../services/markService';
import { examService } from '../../services/examService';
import { studentService } from '../../services/studentService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

export const AdminMarksPage = () => {
  const [marks, setMarks] = useState([]);
  const [exams, setExams] = useState([]);
  const [students, setStudents] = useState([]);
  const [selectedExamId, setSelectedExamId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Form Modal
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

  useEffect(() => {
    Promise.all([
      examService.getAdminExams({ size: 100 }),
      studentService.getStudents({ size: 100 }),
    ]).then(([eRes, sRes]) => {
      setExams(eRes.data?.content || []);
      setStudents(sRes.data?.content || []);
    }).catch(() => {});
  }, []);

  const fetchMarks = async (pageNumber = 0, examId = selectedExamId) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (examId) params.examId = examId;
      const res = await markService.getAdminMarks(params);
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
    setModalExamId(exams[0]?.examId ? String(exams[0].examId) : '');
    setModalStudentId(students[0]?.studentId ? String(students[0].studentId) : '');
    setMarksObtained('');
    setRemarks('');
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (m) => {
    setIsEditing(true);
    setEditingId(m.markId);
    setModalExamId(String(m.examId));
    setModalStudentId(String(m.studentId));
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
        await markService.updateAdminMark(editingId, {
          marksObtained: Number(marksObtained),
          remarks: remarks.trim() || undefined,
        });
      } else {
        await markService.createAdminMark({
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
      await markService.deleteAdminMark(deletingId);
      setIsDeleteDialogOpen(false);
      fetchMarks(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete mark record.');
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
        <span className="inline-block px-2.5 py-0.5 rounded text-xs font-bold bg-slate-100 text-slate-800">
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
            aria-label="Edit mark"
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
            aria-label="Delete mark"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Student Marks & Grades Administration"
        subtitle="Manage academic scores, grade assignments, and official performance records"
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
        emptyMessage="No evaluation records found matching criteria."
        keyField="markId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchMarks(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Modify Grade / Score' : 'Record Student Score'}
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
                options={students.map((s) => ({ value: String(s.studentId), label: `${s.rollNumber} - ${s.firstName} ${s.lastName || ''}` }))}
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
          />

          <Input
            label="Remarks (Optional)"
            name="remarks"
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="Feedback or re-evaluation note..."
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Record Marks'}
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
        message="Are you sure you want to permanently remove this student mark entry?"
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminMarksPage;
