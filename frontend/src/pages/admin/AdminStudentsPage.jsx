import React, { useState, useEffect } from 'react';
import { studentService } from '../../services/studentService';
import { programService } from '../../services/programService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import SearchBar from '../../components/common/SearchBar';
import StatusBadge from '../../components/common/Badge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

export const AdminStudentsPage = () => {
  const [students, setStudents] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProgramId, setSelectedProgramId] = useState('');
  const [selectedSemester, setSelectedSemester] = useState('');

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    programId: '',
    rollNumber: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    admissionYear: 2024,
    currentSemester: 1,
    section: 'A',
    gender: 'MALE',
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete Dialog
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    programService.getAllPrograms()
      .then((res) => setPrograms(res.data || []))
      .catch(() => {});
  }, []);

  const fetchStudents = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedProgramId) params.programId = selectedProgramId;
      if (selectedSemester) params.semester = selectedSemester;
      if (searchTerm) params.search = searchTerm;
      const res = await studentService.getStudents(params);
      const pageData = res.data;
      setStudents(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load students.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents(0);
  }, [selectedProgramId, selectedSemester, searchTerm]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      programId: programs[0]?.programId ? String(programs[0].programId) : '',
      rollNumber: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      admissionYear: 2024,
      currentSemester: 1,
      section: 'A',
      gender: 'MALE',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (s) => {
    setIsEditing(true);
    setEditingId(s.studentId);
    setFormData({
      programId: String(s.programId),
      rollNumber: s.rollNumber,
      firstName: s.firstName,
      lastName: s.lastName || '',
      email: s.email || '',
      phone: s.phoneNumber || s.phone || '',
      admissionYear: s.admissionYear || 2024,
      currentSemester: s.currentSemester || 1,
      section: s.section || 'A',
      gender: s.gender || 'MALE',
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
        programId: Number(formData.programId),
        admissionYear: Number(formData.admissionYear),
        currentSemester: Number(formData.currentSemester),
      };

      if (isEditing) {
        await studentService.updateStudent(editingId, payload);
      } else {
        await studentService.createStudent(payload);
      }
      setIsModalOpen(false);
      fetchStudents(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save student record.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await studentService.deleteStudent(deletingId);
      setIsDeleteDialogOpen(false);
      fetchStudents(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete student.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Roll Number', accessor: 'rollNumber', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Name', render: (r) => `${r.firstName} ${r.lastName || ''}` },
    { header: 'Program', accessor: 'programCode' },
    { header: 'Sem / Sec', render: (r) => `Sem ${r.currentSemester} - ${r.section}`, align: 'center' },
    { header: 'Email', accessor: 'email', cellClassName: 'text-xs text-slate-500' },
    { header: 'Status', render: (r) => <StatusBadge status={r.active ? 'ACTIVE' : 'INACTIVE'} /> },
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
            aria-label="Edit student"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.studentId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete student"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Student Management"
        subtitle="Maintain department student master data, cohorts, and academic standing"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Add Student
        </Button>
      </PageHeader>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Search by name or roll no..."
        />
        <Select
          placeholder="All Programs"
          value={selectedProgramId}
          onChange={(e) => setSelectedProgramId(e.target.value)}
          options={programs.map((p) => ({ value: String(p.programId), label: `${p.code} - ${p.name}` }))}
        />
        <Select
          placeholder="All Semesters"
          value={selectedSemester}
          onChange={(e) => setSelectedSemester(e.target.value)}
          options={[1, 2, 3, 4, 5, 6, 7, 8].map((s) => ({ value: String(s), label: `Semester ${s}` }))}
        />
      </div>

      <Table
        columns={columns}
        data={students}
        loading={loading}
        error={error}
        onRetry={() => fetchStudents(page)}
        emptyMessage="No students found matching current filters."
        keyField="studentId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchStudents(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Edit Student Details' : 'Register New Student'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Roll Number"
              name="rollNumber"
              required
              value={formData.rollNumber}
              onChange={(e) => setFormData({ ...formData, rollNumber: e.target.value })}
              placeholder="e.g. 23AIML001"
            />
            <Select
              label="Program"
              name="programId"
              required
              value={formData.programId}
              onChange={(e) => setFormData({ ...formData, programId: e.target.value })}
              options={programs.map((p) => ({ value: String(p.programId), label: p.code }))}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="First Name"
              name="firstName"
              required
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
            />
            <Input
              label="Last Name"
              name="lastName"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
            <Input
              label="Phone"
              name="phone"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-3 gap-3">
            <Input
              label="Admission Year"
              name="admissionYear"
              type="number"
              required
              value={formData.admissionYear}
              onChange={(e) => setFormData({ ...formData, admissionYear: e.target.value })}
            />
            <Select
              label="Current Semester"
              name="currentSemester"
              required
              value={String(formData.currentSemester)}
              onChange={(e) => setFormData({ ...formData, currentSemester: e.target.value })}
              options={[1, 2, 3, 4, 5, 6, 7, 8].map((s) => ({ value: String(s), label: `Semester ${s}` }))}
            />
            <Input
              label="Section"
              name="section"
              required
              value={formData.section}
              onChange={(e) => setFormData({ ...formData, section: e.target.value })}
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Create Student'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Deactivate / Delete Student"
        message="Are you sure you want to delete this student record? This action will impact linked enrollments and attendance."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminStudentsPage;
