import React, { useState, useEffect } from 'react';
import { programService } from '../../services/programService';
import { departmentService } from '../../services/departmentService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/Badge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';

export const AdminProgramsPage = () => {
  const [programs, setPrograms] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    departmentId: '',
    programCode: '',
    programName: '',
    durationYears: 4,
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    departmentService.getAllDepartments()
      .then((res) => setDepartments(res.data || []))
      .catch(() => {});
  }, []);

  const fetchPrograms = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await programService.getAllPrograms();
      setPrograms(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load programs.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPrograms();
  }, []);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      departmentId: departments[0]?.departmentId ? String(departments[0].departmentId) : '',
      programCode: '',
      programName: '',
      durationYears: 4,
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (p) => {
    setIsEditing(true);
    setEditingId(p.programId);
    setFormData({
      departmentId: String(p.departmentId),
      programCode: p.programCode || p.code,
      programName: p.programName || p.name,
      durationYears: p.durationYears || 4,
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
        durationYears: Number(formData.durationYears),
      };

      if (isEditing) {
        await programService.updateProgram(editingId, payload);
      } else {
        await programService.createProgram(payload);
      }
      setIsModalOpen(false);
      fetchPrograms();
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save program.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await programService.deleteProgram(deletingId);
      setIsDeleteDialogOpen(false);
      fetchPrograms();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete program.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Code', accessor: 'code', render: (r) => r.code || r.programCode, cellClassName: 'font-mono font-semibold text-xs' },
    { header: 'Program Name', accessor: 'name', render: (r) => r.name || r.programName },
    { header: 'Department', accessor: 'departmentName' },
    { header: 'Duration', accessor: 'durationYears', render: (r) => `${r.durationYears} Years`, align: 'center' },
    { header: 'Status', render: (r) => <StatusBadge status={r.active !== false ? 'ACTIVE' : 'INACTIVE'} /> },
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
            aria-label="Edit program"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.programId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete program"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Academic Degree Programs"
        subtitle="Undergraduate and postgraduate degree programs (AIML, IOT, RAI)"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Add Program
        </Button>
      </PageHeader>

      <Table
        columns={columns}
        data={programs}
        loading={loading}
        error={error}
        onRetry={fetchPrograms}
        emptyMessage="No programs registered in the department."
        keyField="programId"
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Edit Program' : 'Register Degree Program'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Program Code"
              name="programCode"
              required
              value={formData.programCode}
              onChange={(e) => setFormData({ ...formData, programCode: e.target.value })}
              placeholder="e.g. AIML, IOT, RAI"
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

          <Input
            label="Program Name"
            name="programName"
            required
            value={formData.programName}
            onChange={(e) => setFormData({ ...formData, programName: e.target.value })}
            placeholder="e.g. Artificial Intelligence & Machine Learning"
          />

          <Input
            label="Duration (Years)"
            name="durationYears"
            type="number"
            min="1"
            max="10"
            required
            value={formData.durationYears}
            onChange={(e) => setFormData({ ...formData, durationYears: e.target.value })}
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Create Program'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Program"
        message="Are you sure you want to delete this program? Enrolled students and curriculum courses will be affected."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminProgramsPage;
