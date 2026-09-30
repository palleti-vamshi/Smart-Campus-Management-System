import React, { useState, useEffect } from 'react';
import { enrollmentService } from '../../services/enrollmentService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import StatusBadge, { CourseTypeBadge } from '../../components/common/Badge';

export const StudentCoursesPage = () => {
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchCourses = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await enrollmentService.getMyEnrollments();
      const list = res?.data?.content || res?.content || res?.data || (Array.isArray(res) ? res : []);
      setEnrollments(Array.isArray(list) ? list : []);
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.message || err.message || 'Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  const columns = [
    {
      header: 'Course',
      render: (r) => (
        <div>
          <div className="font-bold text-sm text-theme-primary">{r.courseName}</div>
          <div className="font-mono text-xs text-theme-brand font-semibold mt-0.5">{r.courseCode}</div>
        </div>
      ),
    },
    {
      header: 'Type',
      accessor: 'courseType',
      align: 'center',
      render: (r) => (
        <CourseTypeBadge label={r.courseType === 'LAB' || r.courseType === 'LABORATORY' ? 'LABORATORY' : 'THEORY'} />
      ),
    },
    {
      header: 'Credits',
      accessor: 'credits',
      align: 'center',
      render: (r) =>
        Number(r.credits) === 0 ? (
          <span className="inline-block px-2 py-0.5 rounded text-[11px] font-bold bg-amber-600 text-white border border-amber-700 shadow-xs">
            0 (Non-Credit)
          </span>
        ) : (
          <span className="font-bold text-sm text-theme-primary font-mono">{Number(r.credits).toFixed(1)}</span>
        ),
    },
    {
      header: 'Semester',
      accessor: 'semester',
      align: 'center',
      render: (r) => <span className="text-xs font-semibold text-theme-secondary">Semester {r.semester}</span>,
    },
    {
      header: 'Faculty',
      accessor: 'facultyName',
      render: (r) => <span className="text-xs font-medium text-theme-primary">{r.facultyName || 'Department Faculty'}</span>,
    },
    {
      header: 'Status',
      accessor: 'status',
      align: 'center',
      render: (r) => <StatusBadge status={r.status || 'ENROLLED'} />,
    },
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
