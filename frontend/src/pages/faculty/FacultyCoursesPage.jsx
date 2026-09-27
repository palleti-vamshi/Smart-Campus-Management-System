import React, { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';

export const FacultyCoursesPage = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchCourses = async () => {
    setLoading(true);
    setError(null);
    try {
      // Courses assigned to faculty are retrieved via the faculty dashboard
      const res = await dashboardService.getFacultyDashboard();
      setCourses(res.data?.assignedCourses || []);
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
    { header: 'Course Code', accessor: 'courseCode', cellClassName: 'font-semibold text-slate-800' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Credits', accessor: 'credits', align: 'center' },
    { header: 'Semester', accessor: 'semester', align: 'center' },
    { header: 'Enrolled Students', accessor: 'enrolledStudents', align: 'center' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="My Assigned Courses"
        subtitle="Department curriculum courses assigned for teaching and instruction"
      />

      <Table
        columns={columns}
        data={courses}
        loading={loading}
        error={error}
        onRetry={fetchCourses}
        emptyMessage="No courses assigned to your profile."
        keyField="courseId"
      />
    </div>
  );
};

export default FacultyCoursesPage;
