import React, { useState, useEffect } from 'react';
import { markService } from '../../services/markService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/Badge';

export const StudentMarksPage = () => {
  const [marks, setMarks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchMarks = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await markService.getMyMarks();
      setMarks(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load marks.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMarks();
  }, []);

  const columns = [
    { header: 'Course Code', accessor: 'courseCode', cellClassName: 'font-semibold' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Exam', accessor: 'examName' },
    { header: 'Exam Type', accessor: 'examType', render: (r) => <StatusBadge status={r.examType} /> },
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
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Results & Marks"
        subtitle="Evaluated assessment performance and official course grades"
      />

      <Table
        columns={columns}
        data={marks}
        loading={loading}
        error={error}
        onRetry={fetchMarks}
        emptyMessage="No evaluation results published yet."
        keyField="markId"
      />
    </div>
  );
};

export default StudentMarksPage;
