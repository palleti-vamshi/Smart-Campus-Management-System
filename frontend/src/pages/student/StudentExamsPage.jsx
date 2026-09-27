import React, { useState, useEffect } from 'react';
import { examService } from '../../services/examService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/Badge';

export const StudentExamsPage = () => {
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchExams = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await examService.getMyExams();
      setExams(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load exams.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExams();
  }, []);

  const columns = [
    { header: 'Course Code', accessor: 'courseCode', cellClassName: 'font-semibold' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Exam Name', accessor: 'examName' },
    { header: 'Exam Type', accessor: 'examType', render: (r) => <StatusBadge status={r.examType} /> },
    { header: 'Date', accessor: 'examDate' },
    { header: 'Time Slot', render: (r) => (r.startTime && r.endTime ? `${r.startTime} - ${r.endTime}` : '-') },
    { header: 'Max Marks', accessor: 'maxMarks', align: 'right' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Examinations"
        subtitle="Scheduled mid-term, end-term, and practical assessments"
      />

      <Table
        columns={columns}
        data={exams}
        loading={loading}
        error={error}
        onRetry={fetchExams}
        emptyMessage="No examinations scheduled for your enrolled courses."
        keyField="examId"
      />
    </div>
  );
};

export default StudentExamsPage;
