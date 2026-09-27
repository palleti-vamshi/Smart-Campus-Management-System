import React, { useState, useEffect } from 'react';
import { facultyService } from '../../services/facultyService';
import { departmentService } from '../../services/departmentService';
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

export const AdminFacultyPage = () => {
  const [facultyList, setFacultyList] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDeptId, setSelectedDeptId] = useState('');

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    departmentId: '',
    employeeCode: '',
    firstName: '',
    lastName: '',
    designation: 'Assistant Professor',
    specialization: '',
    email: '',
    phone: '',
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete Dialog
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    departmentService.getAllDepartments()
      .then((res) => setDepartments(res.data || []))
      .catch(() => {});
  }, []);

  const fetchFaculty = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedDeptId) params.departmentId = selectedDeptId;
      if (searchTerm) params.search = searchTerm;
      const res = await facultyService.getFaculty(params);
      const pageData = res.data;
      setFacultyList(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load faculty.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFaculty(0);
  }, [selectedDeptId, searchTerm]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      departmentId: departments[0]?.departmentId ? String(departments[0].departmentId) : '',
      employeeCode: '',
      firstName: '',
      lastName: '',
      designation: 'Assistant Professor',
      specialization: '',
      email: '',
      phone: '',
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (f) => {
    setIsEditing(true);
    setEditingId(f.facultyId);
    setFormData({
      departmentId: String(f.departmentId),
      employeeCode: f.employeeCode,
      firstName: f.firstName,
      lastName: f.lastName || '',
      designation: f.designation || 'Assistant Professor',
      specialization: f.specialization || '',
      email: f.email || '',
      phone: f.phoneNumber || f.phone || '',
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
        departmentId: Number(formData.departmentId),
      };

      if (isEditing) {
        await facultyService.updateFaculty(editingId, payload);
      } else {
        await facultyService.createFaculty(payload);
      }
      setIsModalOpen(false);
      fetchFaculty(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save faculty record.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await facultyService.deleteFaculty(deletingId);
      setIsDeleteDialogOpen(false);
      fetchFaculty(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete faculty.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Employee Code', accessor: 'employeeCode', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Name', render: (r) => `${r.firstName} ${r.lastName || ''}` },
    { header: 'Designation', accessor: 'designation' },
    { header: 'Department', accessor: 'departmentName' },
    { header: 'Specialization', accessor: 'specialization', cellClassName: 'text-xs text-slate-500' },
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
            aria-label="Edit faculty"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.facultyId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete faculty"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Faculty Management"
        subtitle="Maintain department professors, designations, and academic assignments"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Add Faculty
        </Button>
      </PageHeader>

      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="w-full sm:w-64">
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search by name or code..."
          />
        </div>
        <div className="w-full sm:w-64">
          <Select
            placeholder="All Departments"
            value={selectedDeptId}
            onChange={(e) => setSelectedDeptId(e.target.value)}
            options={departments.map((d) => ({ value: String(d.departmentId), label: d.name }))}
          />
        </div>
      </div>

      <Table
        columns={columns}
        data={facultyList}
        loading={loading}
        error={error}
        onRetry={() => fetchFaculty(page)}
        emptyMessage="No faculty records found matching current criteria."
        keyField="facultyId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchFaculty(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Edit Faculty Member' : 'Register New Faculty Member'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Employee Code"
              name="employeeCode"
              required
              value={formData.employeeCode}
              onChange={(e) => setFormData({ ...formData, employeeCode: e.target.value })}
              placeholder="e.g. FAC001"
            />
            <Select
              label="Department"
              name="departmentId"
              required
              value={formData.departmentId}
              onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
              options={departments.map((d) => ({ value: String(d.departmentId), label: d.name }))}
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
              label="Designation"
              name="designation"
              required
              value={formData.designation}
              onChange={(e) => setFormData({ ...formData, designation: e.target.value })}
              placeholder="e.g. Associate Professor"
            />
            <Input
              label="Specialization"
              name="specialization"
              value={formData.specialization}
              onChange={(e) => setFormData({ ...formData, specialization: e.target.value })}
              placeholder="e.g. Computer Vision, IoT"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Email"
              name="email"
              type="email"
              required
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

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Create Faculty'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Deactivate / Delete Faculty Member"
        message="Are you sure you want to delete this faculty member? Assigned courses and timetable slots will be unassigned."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminFacultyPage;
