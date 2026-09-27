import React, { useState, useEffect } from 'react';
import { classroomService } from '../../services/classroomService';
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

export const AdminClassroomsPage = () => {
  const [classrooms, setClassrooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Form Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    roomNumber: '',
    building: '',
    roomType: 'LECTURE_HALL',
    capacity: 60,
    isActive: true,
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const fetchClassrooms = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const res = await classroomService.getClassrooms({ page: pageNumber, size: 20 });
      const pageData = res.data;
      setClassrooms(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load classrooms.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClassrooms(0);
  }, []);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      roomNumber: '',
      building: 'Academic Block A',
      roomType: 'LECTURE_HALL',
      capacity: 60,
      isActive: true,
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (c) => {
    setIsEditing(true);
    setEditingId(c.classroomId);
    setFormData({
      roomNumber: c.roomNumber,
      building: c.building,
      roomType: c.roomType || 'LECTURE_HALL',
      capacity: c.capacity,
      isActive: c.active !== false,
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
        capacity: Number(formData.capacity),
      };

      if (isEditing) {
        await classroomService.updateClassroom(editingId, payload);
      } else {
        await classroomService.createClassroom(payload);
      }
      setIsModalOpen(false);
      fetchClassrooms(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save classroom.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await classroomService.deleteClassroom(deletingId);
      setIsDeleteDialogOpen(false);
      fetchClassrooms(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete classroom.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Room Number', accessor: 'roomNumber', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Building', accessor: 'building' },
    { header: 'Room Type', accessor: 'roomType', render: (r) => <StatusBadge status={r.roomType} /> },
    { header: 'Capacity', accessor: 'capacity', align: 'center', render: (r) => `${r.capacity} Seats` },
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
            aria-label="Edit classroom"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.classroomId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete classroom"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Classroom Infrastructure"
        subtitle="Manage lecture halls, smart classrooms, research labs, and seating capacities"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Add Classroom
        </Button>
      </PageHeader>

      <Table
        columns={columns}
        data={classrooms}
        loading={loading}
        error={error}
        onRetry={() => fetchClassrooms(page)}
        emptyMessage="No classrooms registered."
        keyField="classroomId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchClassrooms(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Modify Classroom Facility' : 'Register New Classroom'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Room Number"
              name="roomNumber"
              required
              value={formData.roomNumber}
              onChange={(e) => setFormData({ ...formData, roomNumber: e.target.value })}
              placeholder="e.g. LH-101, LAB-202"
            />
            <Input
              label="Building"
              name="building"
              required
              value={formData.building}
              onChange={(e) => setFormData({ ...formData, building: e.target.value })}
              placeholder="e.g. Science & Tech Block"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Room Type"
              name="roomType"
              required
              value={formData.roomType}
              onChange={(e) => setFormData({ ...formData, roomType: e.target.value })}
              options={[
                { value: 'LECTURE_HALL', label: 'Lecture Hall' },
                { value: 'LAB', label: 'Laboratory' },
                { value: 'SEMINAR_HALL', label: 'Seminar Hall' },
                { value: 'SMART_CLASSROOM', label: 'Smart Classroom' },
              ]}
            />
            <Input
              label="Seating Capacity"
              name="capacity"
              type="number"
              min="1"
              required
              value={formData.capacity}
              onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Register Room'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Classroom"
        message="Are you sure you want to delete this classroom facility? Linked timetable schedules may be invalidated."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminClassroomsPage;
