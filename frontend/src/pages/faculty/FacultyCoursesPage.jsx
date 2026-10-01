import React, { useState, useEffect } from 'react';
import { courseService } from '../../services/courseService';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import { CourseTypeBadge, SectionBadge } from '../../components/common/Badge';

export const FacultyCoursesPage = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchCourses = async () => {
    setLoading(true);
    setError(null);
    try {
      // First try dedicated faculty courses endpoint, fallback to dashboard
      try {
        const res = await courseService.getFacultyCourses();
        if (res.data && Array.isArray(res.data) && res.data.length > 0) {
          setCourses(res.data);
          return;
        }
      } catch (e) {
        // fallback
      }

      const dashRes = await dashboardService.getFacultyDashboard();
      setCourses(dashRes.data?.assignedCourses || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load assigned courses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  const columns = [
    {
      header: 'Course Code',
      accessor: 'courseCode',
      cellClassName: 'font-mono font-bold text-theme-primary',
    },
    {
      header: 'Course Name',
      accessor: 'courseName',
      render: (r) => (
        <div>
          <span className="font-semibold text-sm text-theme-primary block">{r.courseName}</span>
          <span className="text-xs text-theme-muted">{r.programCode || 'AIML'} Department</span>
        </div>
      ),
    },
    {
      header: 'Type',
      align: 'center',
      render: (r) => {
        const isLab =
          (r.courseType && String(r.courseType).toUpperCase().includes('LAB')) ||
          (r.courseName && r.courseName.toUpperCase().includes('LAB'));
        return <CourseTypeBadge type={isLab ? 'LABORATORY' : 'THEORY'} />;
      },
    },
    {
      header: 'Section',
      align: 'center',
      render: (r) => <SectionBadge section={r.section} />,
    },
    {
      header: 'Program',
      align: 'center',
      render: (r) => (
        <span className="text-xs font-semibold px-2 py-0.5 rounded bg-theme-elevated text-theme-primary border border-theme">
          {r.programCode || 'AIML'}
        </span>
      ),
    },
    {
      header: 'Credits',
      accessor: 'credits',
      align: 'center',
      render: (r) => (
        <span className="font-bold text-xs text-theme-primary">
          {Number(r.credits || 0).toFixed(1)}
        </span>
      ),
    },
    {
      header: 'Semester',
      accessor: 'semester',
      align: 'center',
      render: (r) => (
        <span className="text-xs font-medium text-theme-secondary">
          Sem {r.semester || 3}
        </span>
      ),
    },
    {
      header: 'Enrolled Students',
      accessor: 'enrolledStudents',
      align: 'center',
      render: (r) => (
        <span className="inline-flex items-center gap-1 font-bold text-xs px-2.5 py-0.5 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
          {r.enrolledStudents != null ? `${r.enrolledStudents} Students` : '-'}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="My Assigned Courses"
        subtitle="Department curriculum courses assigned to your profile for instruction and assessment"
      />

      <Table
        columns={columns}
        data={courses}
        loading={loading}
        error={error}
        onRetry={fetchCourses}
        emptyMessage="No courses are currently assigned to your profile."
        keyField="courseId"
      />
    </div>
  );
};

export default FacultyCoursesPage;
