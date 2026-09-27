import React, { useState, useEffect } from 'react';
import { documentService } from '../../services/documentService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/Badge';
import { FiDownload, FiPlus, FiFileText } from 'react-icons/fi';

export const StudentDocumentsPage = () => {
  const [types, setTypes] = useState([]);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // New Request Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTypeId, setSelectedTypeId] = useState('');
  const [purpose, setPurpose] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [typesRes, reqsRes] = await Promise.all([
        documentService.getDocumentTypes(),
        documentService.getMyRequests(),
      ]);
      setTypes(typesRes.data || []);
      const reqList = reqsRes.data?.content || reqsRes.data || [];
      setRequests(reqList);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load documents data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleOpenModal = (typeId = '') => {
    setSelectedTypeId(typeId ? String(typeId) : (types[0]?.documentTypeId ? String(types[0].documentTypeId) : ''));
    setPurpose('');
    setRemarks('');
    setFormError('');
    setIsModalOpen(true);
  };

  const handleDownload = async (requestId, requestNumber) => {
    try {
      const response = await documentService.downloadCertificate(requestId);
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
      alert(err.response?.data?.message || 'Failed to download certificate.');
    }
  };

  const handleSubmitRequest = async (e) => {
    e.preventDefault();
    if (!selectedTypeId) {
      setFormError('Please select a document type.');
      return;
    }
    if (!purpose.trim()) {
      setFormError('Please specify the purpose of the request.');
      return;
    }

    setSubmitting(true);
    setFormError('');
    try {
      await documentService.requestDocument({
        documentTypeId: Number(selectedTypeId),
        purpose: purpose.trim(),
        remarks: remarks.trim() || undefined,
      });
      setIsModalOpen(false);
      setSuccessMsg('Document request submitted successfully!');
      setTimeout(() => setSuccessMsg(''), 4000);
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to submit document request.');
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { header: 'Request ID', accessor: 'requestNumber', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Document', accessor: 'documentTypeName' },
    { header: 'Purpose', accessor: 'purpose' },
    { header: 'Submitted Date', accessor: 'createdAt', render: (r) => r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '-' },
    { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status} /> },
    {
      header: 'Actions',
      align: 'right',
      render: (r) =>
        r.status === 'ISSUED' ? (
          <Button
            size="sm"
            variant="outline"
            onClick={() => handleDownload(r.requestId, r.requestNumber)}
            icon={FiDownload}
          >
            Download
          </Button>
        ) : (
          <span className="text-xs text-slate-400">Processing</span>
        ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Digital Documents & Certificates"
        subtitle="Request official bonafide certificates, grade sheets, and transcripts"
      >
        <Button
          variant="primary"
          onClick={() => handleOpenModal()}
          icon={FiPlus}
        >
          New Request
        </Button>
      </PageHeader>

      {successMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-lg font-medium">
          {successMsg}
        </div>
      )}

      {/* Available Documents Section */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <h2 className="text-base font-bold text-slate-800 mb-4">Available Certificates</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {types.map((t) => (
            <div
              key={t.documentTypeId}
              className="p-4 rounded-xl border border-slate-200 hover:border-primary-400 hover:shadow-xs transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <div className="p-2 rounded-lg bg-primary-50 text-primary-600">
                    <FiFileText className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-sm">{t.name}</h3>
                    <p className="text-xs text-slate-400">{t.documentTypeCode}</p>
                  </div>
                </div>
                <p className="text-xs text-slate-600 mb-3">{t.description || 'Official verified university document.'}</p>
              </div>
              <Button
                size="sm"
                variant="outline"
                className="w-full mt-2"
                onClick={() => handleOpenModal(t.documentTypeId)}
              >
                Request Certificate
              </Button>
            </div>
          ))}
        </div>
      </div>

      {/* My Requests Section */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <h2 className="text-base font-bold text-slate-800 mb-4">My Requests & History</h2>
        <Table
          columns={columns}
          data={requests}
          loading={loading}
          error={error}
          onRetry={fetchData}
          emptyMessage="You have not submitted any certificate requests yet."
          keyField="requestId"
        />
      </div>

      {/* New Request Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Request Official Certificate"
      >
        <form onSubmit={handleSubmitRequest} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {formError}
            </div>
          )}

          <Select
            label="Document Type"
            name="documentType"
            required
            value={selectedTypeId}
            onChange={(e) => setSelectedTypeId(e.target.value)}
            options={types.map((t) => ({ value: String(t.documentTypeId), label: `${t.name} (${t.documentTypeCode})` }))}
          />

          <Input
            label="Purpose of Request"
            name="purpose"
            required
            value={purpose}
            onChange={(e) => setPurpose(e.target.value)}
            placeholder="e.g. Visa Application, Internship Verification, Bank Loan"
          />

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
              Additional Details / Remarks
            </label>
            <textarea
              rows={3}
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="Any specific instructions or submission requirements..."
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={submitting}>
              Submit Request
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default StudentDocumentsPage;
