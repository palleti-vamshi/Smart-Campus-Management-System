import React, { useState, useEffect } from 'react';
import { documentService, formatDocumentName } from '../../services/documentService';
import { useToast } from '../../context/ToastContext';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/Badge';
import { FiDownload, FiPlus, FiFileText, FiCheck, FiClock, FiAlertCircle, FiX } from 'react-icons/fi';
import { formatDateDDMMYYYY } from '../../utils/academicCalendar';

// Status progression stages
const PROGRESS_STAGES = ['SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'ISSUED'];

const StatusStepper = ({ currentStatus }) => {
  if (currentStatus === 'REJECTED') {
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-950 dark:bg-rose-950/50 dark:text-rose-200 border border-rose-300 dark:border-rose-800">
        <FiX className="w-3.5 h-3.5 text-rose-600" /> REJECTED
      </span>
    );
  }

  const currentIndex = PROGRESS_STAGES.indexOf(currentStatus);

  return (
    <div className="flex items-center gap-1.5 py-1">
      {PROGRESS_STAGES.map((stage, idx) => {
        const isCompleted = currentIndex > idx;
        const isCurrent = currentIndex === idx;

        let badgeStyle = 'bg-slate-200 text-slate-800 border border-slate-300 dark:bg-slate-700 dark:text-white dark:border-slate-600 font-semibold';
        if (isCurrent) {
          badgeStyle = 'bg-amber-600 text-white border border-amber-700 font-extrabold shadow-2xs';
        } else if (isCompleted) {
          badgeStyle = 'bg-emerald-600 text-white border border-emerald-700 font-bold';
        }

        return (
          <React.Fragment key={stage}>
            <div
              className={`flex items-center gap-1 px-2 py-0.5 rounded text-[10px] tracking-wider uppercase transition-colors ${badgeStyle}`}
              title={stage.replace('_', ' ')}
            >
              {isCompleted ? <FiCheck className="w-3 h-3 text-white" /> : null}
              <span>{stage === 'UNDER_REVIEW' ? 'REVIEW' : stage}</span>
            </div>
            {idx < PROGRESS_STAGES.length - 1 && (
              <span className={`text-[10px] font-bold ${currentIndex > idx ? 'text-emerald-700' : 'text-slate-400'}`}>
                →
              </span>
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
};

export const StudentDocumentsPage = () => {
  const [types, setTypes] = useState([]);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { showToast } = useToast();

  // New Request Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTypeId, setSelectedTypeId] = useState('');
  const [purpose, setPurpose] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [typesRes, reqsRes] = await Promise.all([
        documentService.getDocumentTypes(),
        documentService.getMyRequests(),
      ]);

      // Debug: log raw responses to trace data flow
      console.log('[SCMS] Document types response:', JSON.stringify(typesRes, null, 2));
      console.log('[SCMS] Document requests response:', JSON.stringify(reqsRes, null, 2));

      // Extract document types — handle multiple possible shapes
      const typesList = Array.isArray(typesRes?.data)
        ? typesRes.data
        : Array.isArray(typesRes?.content)
        ? typesRes.content
        : Array.isArray(typesRes)
        ? typesRes
        : [];

      console.log('[SCMS] Extracted types list:', typesList.length, 'items', typesList.map(t => t.displayName || t.documentName));
      setTypes(typesList);

      // Extract document requests — handle nested pagination or flat array
      let reqList = [];
      if (Array.isArray(reqsRes?.data?.content)) {
        reqList = reqsRes.data.content;
      } else if (Array.isArray(reqsRes?.content)) {
        reqList = reqsRes.content;
      } else if (Array.isArray(reqsRes?.data)) {
        reqList = reqsRes.data;
      } else if (Array.isArray(reqsRes)) {
        reqList = reqsRes;
      }
      console.log('[SCMS] Extracted requests list:', reqList.length, 'items');
      setRequests(reqList);
    } catch (err) {
      console.error('[SCMS] fetchData error:', err);
      setError(err.friendlyMessage || err.response?.data?.message || err.message || 'Failed to load documents data.');
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
      showToast('Generating official certificate download...', 'info', 2000);
      const response = await documentService.downloadCertificate(requestId);
      const blob = response.data instanceof Blob ? response.data : new Blob([response.data], { type: 'application/pdf' });

      let filename = `certificate-${requestNumber || requestId}.pdf`;
      const disposition = response.headers?.['content-disposition'] || response.headers?.get?.('content-disposition');
      if (disposition && disposition.includes('filename=')) {
        const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (match && match[1]) {
          filename = match[1].replace(/['"]/g, '').trim();
        }
      }

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();

      setTimeout(() => {
        window.URL.revokeObjectURL(url);
        if (document.body.contains(a)) {
          document.body.removeChild(a);
        }
      }, 2500);

      showToast('Certificate downloaded successfully!', 'success');
    } catch (err) {
      console.error('Download error:', err);
      let msg = 'Failed to download certificate.';
      if (err.response?.data instanceof Blob) {
        try {
          const text = await err.response.data.text();
          const parsed = JSON.parse(text);
          msg = parsed.message || msg;
        } catch (_) {}
      } else if (err.friendlyMessage || err.response?.data?.message || err.message) {
        msg = err.friendlyMessage || err.response?.data?.message || err.message;
      }
      showToast(msg, 'error');
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
        additionalDetails: remarks.trim() || undefined,
        remarks: remarks.trim() || undefined,
      });
      setIsModalOpen(false);
      showToast('Document request submitted successfully!', 'success');
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to submit document request.');
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { header: 'Request ID', accessor: 'requestNumber', cellClassName: 'font-mono text-xs font-bold text-theme-brand' },
    {
      header: 'Document',
      accessor: 'documentTypeName',
      cellClassName: 'font-semibold',
      render: (r) => formatDocumentName(r.documentTypeName),
    },
    { header: 'Purpose', accessor: 'purpose' },
    {
      header: 'Submitted',
      accessor: 'createdAt',
      render: (r) => (
        <span className="font-mono text-xs font-semibold text-theme-primary">
          {r.createdAt ? formatDateDDMMYYYY(r.createdAt) : '-'}
        </span>
      ),
    },
    {
      header: 'Status Progression',
      accessor: 'status',
      render: (r) => <StatusStepper currentStatus={r.status} />,
    },
    {
      header: 'Actions',
      align: 'right',
      render: (r) =>
        r.status === 'ISSUED' ? (
          <Button
            size="sm"
            variant="outline"
            className="font-bold border-rose-800 text-rose-900 bg-rose-50 hover:bg-rose-900 hover:text-white dark:bg-rose-950/40 dark:text-rose-200 dark:border-rose-700"
            onClick={() => handleDownload(r.requestId, r.requestNumber)}
            icon={FiDownload}
          >
            Download PDF
          </Button>
        ) : (
          <span className="text-xs text-theme-secondary font-medium px-2 py-1 rounded bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">In Queue</span>
        ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Digital Documents & Certificates"
        subtitle="Request official bonafide certificates, transcripts, and verified campus credentials"
      >
        <Button
          variant="primary"
          onClick={() => handleOpenModal()}
          icon={FiPlus}
        >
          Request Document
        </Button>
      </PageHeader>

      {/* Available Documents Section */}
      <div className="bg-theme-surface p-5 sm:p-6 rounded-2xl border border-theme shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-base font-bold text-theme-primary">Available Certificates</h2>
            <p className="text-xs text-theme-secondary mt-0.5">Click any document to start your application</p>
          </div>
          <span className="text-xs font-semibold text-theme-muted">{types.length} Document Types</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {types.map((t) => (
            <div
              key={t.documentTypeId || t.id}
              className="p-5 rounded-xl border border-theme bg-theme-elevated hover:border-theme-primary transition-all flex flex-col justify-between shadow-xs hover:shadow-md"
            >
              <div>
                <div className="flex items-center gap-3 mb-2.5">
                  <div className="p-2.5 rounded-xl bg-theme-primary-light text-theme-brand border border-theme">
                    <FiFileText className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-theme-primary text-sm leading-tight">
                      {t.displayName || t.formattedName || formatDocumentName(t.documentName || t.name)}
                    </h3>
                    <p className="text-[11px] font-mono font-semibold text-theme-muted">
                      {t.documentName || t.documentTypeCode || 'OFFICIAL'}
                    </p>
                  </div>
                </div>
                <p className="text-xs text-theme-secondary mb-4 leading-relaxed">
                  {t.description || 'Official verified university credential issued digitally with verification seal.'}
                </p>
              </div>
              <Button
                size="sm"
                variant="outline"
                className="w-full mt-2"
                onClick={() => handleOpenModal(t.documentTypeId || t.id)}
              >
                Request Certificate
              </Button>
            </div>
          ))}
        </div>
      </div>

      {/* My Requests Section */}
      <div className="bg-theme-surface p-5 sm:p-6 rounded-2xl border border-theme shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-base font-bold text-theme-primary">My Requests & Verification History</h2>
            <p className="text-xs text-theme-secondary mt-0.5">Track live review status from department authorities</p>
          </div>
        </div>

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
        footer={
          <div className="flex items-center justify-end gap-3 w-full">
            <Button variant="ghost" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              loading={submitting}
              onClick={handleSubmitRequest}
            >
              Submit Request
            </Button>
          </div>
        }
      >
        <form onSubmit={handleSubmitRequest} className="space-y-4">
          {formError && (
            <div className="p-3.5 bg-theme-elevated border border-rose-300 dark:border-rose-700 text-rose-700 dark:text-rose-300 text-xs rounded-xl flex items-center gap-2 font-medium shadow-xs">
              <FiAlertCircle className="w-4 h-4 shrink-0 text-rose-600 dark:text-rose-400" />
              <span>{formError}</span>
            </div>
          )}

          <Select
            label="Document Type"
            name="documentType"
            required
            value={selectedTypeId}
            onChange={(e) => setSelectedTypeId(e.target.value)}
            options={types.map((t) => ({
              value: String(t.documentTypeId || t.id),
              label: t.displayName || t.formattedName || formatDocumentName(t.documentName || t.name),
            }))}
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
            <label className="block text-xs font-semibold uppercase tracking-wider text-theme-secondary mb-1.5">
              Additional Details / Remarks
            </label>
            <textarea
              rows={3}
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="Any specific instructions or submission requirements..."
              className="w-full text-sm rounded-lg border border-theme bg-theme-surface text-theme-primary p-3 focus:outline-none focus:ring-2 focus:ring-theme focus:border-theme-primary transition-all hover:border-slate-400"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default StudentDocumentsPage;
