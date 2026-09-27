import React, { useState, useEffect } from 'react';
import { enrollmentService } from '../../services/enrollmentService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/Badge';

export const StudentCoursesPage = () => {
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchCourses = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await enrollmentService.getMyEnrollments();
      setEnrollments(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  const columns = [
    { header: 'Course Code', accessor: 'courseCode', cellClassName: 'font-semibold' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Credits', accessor: 'credits', align: 'center' },
    { header: 'Semester', accessor: 'semester', align: 'center' },
    { header: 'Faculty', accessor: 'facultyName' },
    { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status || 'ACTIVE'} /> },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="My Courses"
        subtitle="Currently registered courses and academic credit weights"
      />

      <Table
        columns={columns}
        data={enrollments}
        loading={loading}
        error={error}
        onRetry={fetchCourses}
        emptyMessage="You are not currently enrolled in any courses."
        keyField="enrollmentId"
      />
    </div>
  );
};

export default StudentCoursesPage;
