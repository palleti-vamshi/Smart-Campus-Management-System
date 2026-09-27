import React, { useState, useEffect } from 'react';
import { documentService } from '../../services/documentService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import SearchBar from '../../components/common/SearchBar';
import StatusBadge from '../../components/common/Badge';
import { FiDownload, FiCheck, FiX, FiCheckCircle, FiFileText, FiPlus } from 'react-icons/fi';

const STATUS_OPTIONS = ['SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'ISSUED', 'REJECTED'];

export const AdminDocumentsPage = () => {
  const [requests, setRequests] = useState([]);
  const [documentTypes, setDocumentTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [selectedStatus, setSelectedStatus] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Status Action Modal
  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
  const [currentRequest, setCurrentRequest] = useState(null);
  const [targetStatus, setTargetStatus] = useState('');
  const [statusRemarks, setStatusRemarks] = useState('');
  const [statusLoading, setStatusLoading] = useState(false);
  const [statusError, setStatusError] = useState('');

  // Manage Types Modal
  const [isTypeModalOpen, setIsTypeModalOpen] = useState(false);
  const [typeName, setTypeName] = useState('');
  const [typeCode, setTypeCode] = useState('');
  const [typeDesc, setTypeDesc] = useState('');
  const [typeLoading, setTypeLoading] = useState(false);
  const [typeError, setTypeError] = useState('');

  const fetchDocumentTypes = async () => {
    try {
      const res = await documentService.getAdminDocumentTypes();
      setDocumentTypes(res.data || []);
    } catch (_) {}
  };

  const fetchRequests = async (pageNumber = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (selectedStatus) params.status = selectedStatus;
      if (searchTerm) params.search = searchTerm;
      const res = await documentService.getAdminRequests(params);
      const pageData = res.data;
      setRequests(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load document requests.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocumentTypes();
    fetchRequests(0);
  }, [selectedStatus, searchTerm]);

  const handleOpenStatusModal = (req, nextStatus) => {
    setCurrentRequest(req);
    setTargetStatus(nextStatus);
    setStatusRemarks('');
    setStatusError('');
    setIsStatusModalOpen(true);
  };

  const handleStatusSubmit = async (e) => {
    e.preventDefault();
    setStatusLoading(true);
    setStatusError('');
    try {
      await documentService.updateRequestStatus(currentRequest.requestId, {
        status: targetStatus,
        remarks: statusRemarks.trim() || undefined,
      });
      setIsStatusModalOpen(false);
      fetchRequests(page);
    } catch (err) {
      setStatusError(err.response?.data?.message || err.message || 'Failed to transition status.');
    } finally {
      setStatusLoading(false);
    }
  };

  const handleDownload = async (requestId, requestNumber) => {
    try {
      const response = await documentService.downloadAdminDocument(requestId);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `certificate-${requestNumber || requestId}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert(err.response?.data?.message || 'Certificate download failed.');
    }
  };

  const handleCreateType = async (e) => {
    e.preventDefault();
    setTypeLoading(true);
    setTypeError('');
    try {
      await documentService.createDocumentType({
        name: typeName.trim(),
        code: typeCode.trim().toUpperCase(),
        description: typeDesc.trim(),
        isActive: true,
      });
      setIsTypeModalOpen(false);
      setTypeName('');
      setTypeCode('');
      setTypeDesc('');
      fetchDocumentTypes();
    } catch (err) {
      setTypeError(err.response?.data?.message || err.message || 'Failed to create document type.');
    } finally {
      setTypeLoading(false);
    }
  };

  const columns = [
    { header: 'Request ID', accessor: 'requestNumber', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Student', render: (r) => `${r.studentName} (${r.studentRollNumber})` },
    { header: 'Document', accessor: 'documentTypeName' },
    { header: 'Purpose', accessor: 'purpose' },
    { header: 'Submitted', accessor: 'createdAt', render: (r) => r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '-' },
    { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status} /> },
    {
      header: 'Actions',
      align: 'right',
      render: (r) => (
        <div className="flex items-center justify-end gap-1.5">
          {r.status === 'SUBMITTED' && (
            <>
              <Button
                size="sm"
                variant="outline"
                onClick={() => handleOpenStatusModal(r, 'UNDER_REVIEW')}
              >
                Review
              </Button>
              <Button
                size="sm"
                variant="text"
                className="text-red-600 hover:bg-red-50"
                onClick={() => handleOpenStatusModal(r, 'REJECTED')}
              >
                Reject
              </Button>
            </>
          )}

          {r.status === 'UNDER_REVIEW' && (
            <>
              <Button
                size="sm"
                variant="primary"
                onClick={() => handleOpenStatusModal(r, 'APPROVED')}
              >
                Approve
              </Button>
              <Button
                size="sm"
                variant="text"
                className="text-red-600 hover:bg-red-50"
                onClick={() => handleOpenStatusModal(r, 'REJECTED')}
              >
                Reject
              </Button>
            </>
          )}

          {r.status === 'APPROVED' && (
            <Button
              size="sm"
              variant="success"
              onClick={() => handleOpenStatusModal(r, 'ISSUED')}
              icon={FiCheckCircle}
            >
              Issue
            </Button>
          )}

          {r.status === 'ISSUED' && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleDownload(r.requestId, r.requestNumber)}
              icon={FiDownload}
            >
              Download PDF
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Digital Documents Administration"
        subtitle="Review student certificate requests, authorize verification, and issue credentials"
      >
        <Button
          variant="outline"
          onClick={() => setIsTypeModalOpen(true)}
          icon={FiPlus}
        >
          Add Document Type
        </Button>
      </PageHeader>

      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="w-full sm:w-64">
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search by student or roll no..."
          />
        </div>
        <div className="w-full sm:w-48">
          <Select
            placeholder="All Statuses"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            options={STATUS_OPTIONS.map((s) => ({ value: s, label: s }))}
          />
        </div>
      </div>

      <Table
        columns={columns}
        data={requests}
        loading={loading}
        error={error}
        onRetry={() => fetchRequests(page)}
        emptyMessage="No document requests found."
        keyField="requestId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchRequests(newPage)}
      />

      {/* Status Transition Modal */}
      <Modal
        isOpen={isStatusModalOpen}
        onClose={() => setIsStatusModalOpen(false)}
        title={`Update Request Status to ${targetStatus}`}
      >
        <form onSubmit={handleStatusSubmit} className="space-y-4">
          {statusError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {statusError}
            </div>
          )}

          <div className="bg-slate-50 p-3 rounded-lg text-xs space-y-1 text-slate-600">
            <p><strong>Request Number:</strong> {currentRequest?.requestNumber}</p>
            <p><strong>Student:</strong> {currentRequest?.studentName} ({currentRequest?.studentRollNumber})</p>
            <p><strong>Document:</strong> {currentRequest?.documentTypeName}</p>
            <p><strong>Purpose:</strong> {currentRequest?.purpose}</p>
          </div>

          <Input
            label="Administrative Remarks (Optional)"
            name="remarks"
            value={statusRemarks}
            onChange={(e) => setStatusRemarks(e.target.value)}
            placeholder={targetStatus === 'REJECTED' ? 'Reason for rejection...' : 'Verification comments...'}
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsStatusModalOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant={targetStatus === 'REJECTED' ? 'danger' : 'primary'}
              loading={statusLoading}
            >
              Confirm Transition
            </Button>
          </div>
        </form>
      </Modal>

      {/* Create Document Type Modal */}
      <Modal
        isOpen={isTypeModalOpen}
        onClose={() => setIsTypeModalOpen(false)}
        title="Add Supported Document Type"
      >
        <form onSubmit={handleCreateType} className="space-y-4">
          {typeError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {typeError}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Type Name"
              name="typeName"
              required
              value={typeName}
              onChange={(e) => setTypeName(e.target.value)}
              placeholder="e.g. Bonafide Certificate"
            />
            <Input
              label="Type Code"
              name="typeCode"
              required
              value={typeCode}
              onChange={(e) => setTypeCode(e.target.value)}
              placeholder="e.g. BONAFIDE"
            />
          </div>

          <Input
            label="Description"
            name="typeDesc"
            value={typeDesc}
            onChange={(e) => setTypeDesc(e.target.value)}
            placeholder="Official purpose and validity notes..."
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsTypeModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={typeLoading}>
              Save Type
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default AdminDocumentsPage;
