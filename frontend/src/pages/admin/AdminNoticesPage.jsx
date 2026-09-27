import React, { useState, useEffect } from 'react';
import { noticeService } from '../../services/noticeService';
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

const CATEGORIES = ['ACADEMIC', 'EXAM', 'EVENT', 'INTERNSHIP', 'PLACEMENT', 'IMPORTANT', 'GENERAL'];
const PRIORITIES = ['NORMAL', 'HIGH', 'URGENT'];

export const AdminNoticesPage = () => {
  const [notices, setNotices] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  // Create / Edit Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState('ACADEMIC');
  const [priority, setPriority] = useState('NORMAL');
  const [targetProgramId, setTargetProgramId] = useState('');
  const [expiryDate, setExpiryDate] = useState('');
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // Delete
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    programService.getAllPrograms()
      .then((res) => setPrograms(res.data || []))
      .catch(() => {});
  }, []);

  const fetchNotices = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedCategory) params.category = selectedCategory;
      if (searchTerm) params.keyword = searchTerm;
      const res = await noticeService.getAdminNotices(params);
      const pageData = res.data;
      setNotices(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load notices.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotices(0);
  }, [selectedCategory, searchTerm]);

  const handleOpenCreate = () => {
    setIsEditing(false);
    setEditingId(null);
    setTitle('');
    setContent('');
    setCategory('ACADEMIC');
    setPriority('NORMAL');
    setTargetProgramId('');
    setExpiryDate('');
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (n) => {
    setIsEditing(true);
    setEditingId(n.noticeId);
    setTitle(n.title);
    setContent(n.content);
    setCategory(n.category);
    setPriority(n.priority);
    setTargetProgramId(n.targetProgramId ? String(n.targetProgramId) : '');
    setExpiryDate(n.expiryDate || '');
    setFormError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');
    try {
      const payload = {
        title: title.trim(),
        content: content.trim(),
        category,
        priority,
        targetProgramId: targetProgramId ? Number(targetProgramId) : undefined,
        expiryDate: expiryDate || undefined,
      };

      if (isEditing) {
        await noticeService.updateAdminNotice(editingId, payload);
      } else {
        await noticeService.createAdminNotice(payload);
      }
      setIsModalOpen(false);
      fetchNotices(page);
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save notice.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleteLoading(true);
    try {
      await noticeService.deleteAdminNotice(deletingId);
      setIsDeleteDialogOpen(false);
      fetchNotices(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete notice.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    { header: 'Title', accessor: 'title', cellClassName: 'font-semibold text-slate-800' },
    { header: 'Category', accessor: 'category', render: (r) => <StatusBadge status={r.category} /> },
    { header: 'Priority', accessor: 'priority', render: (r) => <StatusBadge status={r.priority} /> },
    { header: 'Published', accessor: 'publishedAt', render: (r) => r.publishedAt || r.publishDate },
    { header: 'Expires', accessor: 'expiryDate', render: (r) => r.expiryDate || 'No Expiry' },
    { header: 'Target', render: (r) => r.targetProgramCode || r.targetProgram || 'All Programs' },
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
            aria-label="Edit notice"
          />
          <Button
            size="sm"
            variant="text"
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
            onClick={() => {
              setDeletingId(r.noticeId);
              setIsDeleteDialogOpen(true);
            }}
            icon={FiTrash2}
            aria-label="Delete notice"
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Notice Centre Administration"
        subtitle="Broadcast department circulars, examinations, events, and job/internship updates"
      >
        <Button
          variant="primary"
          onClick={handleOpenCreate}
          icon={FiPlus}
        >
          Create Notice
        </Button>
      </PageHeader>

      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="w-full sm:w-64">
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search circulars..."
          />
        </div>
        <div className="w-full sm:w-48">
          <Select
            placeholder="All Categories"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            options={CATEGORIES.map((c) => ({ value: c, label: c }))}
          />
        </div>
      </div>

      <Table
        columns={columns}
        data={notices}
        loading={loading}
        error={error}
        onRetry={() => fetchNotices(page)}
        emptyMessage="No notices found matching current filters."
        keyField="noticeId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchNotices(newPage)}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={isEditing ? 'Modify Circular Notice' : 'Publish Department Circular'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <Input
            label="Notice Title"
            name="title"
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Campus Recruitment Drive: Registration Open"
          />

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Category"
              name="category"
              required
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              options={CATEGORIES.map((c) => ({ value: c, label: c }))}
            />
            <Select
              label="Priority"
              name="priority"
              required
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
              options={PRIORITIES.map((p) => ({ value: p, label: p }))}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Target Program (Optional)"
              name="targetProgramId"
              placeholder="All Department Programs"
              value={targetProgramId}
              onChange={(e) => setTargetProgramId(e.target.value)}
              options={programs.map((prog) => ({ value: String(prog.programId), label: `${prog.code} - ${prog.name}` }))}
            />
            <Input
              label="Expiry Date (Optional)"
              name="expiryDate"
              type="date"
              value={expiryDate}
              onChange={(e) => setExpiryDate(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
              Notice Content *
            </label>
            <textarea
              rows={4}
              required
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Official notification text..."
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {isEditing ? 'Save Changes' : 'Broadcast Notice'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Circular Notice"
        message="Are you sure you want to delete this notice? It will be removed from student and faculty notice boards."
        loading={deleteLoading}
      />
    </div>
  );
};

export default AdminNoticesPage;
