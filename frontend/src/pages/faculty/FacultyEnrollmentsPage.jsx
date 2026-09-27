import React, { useState, useEffect } from 'react';
import { enrollmentService } from '../../services/enrollmentService';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Select from '../../components/common/Select';
import StatusBadge from '../../components/common/Badge';

export const FacultyEnrollmentsPage = () => {
  const [enrollments, setEnrollments] = useState([]);
  const [courses, setCourses] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Fetch faculty assigned courses for filter dropdown
  useEffect(() => {
    dashboardService.getFacultyDashboard()
      .then((res) => {
        setCourses(res.data?.assignedCourses || []);
      })
      .catch(() => {});
  }, []);

  const fetchEnrollments = async (pageNumber = 0, courseId = selectedCourseId) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 20 };
      if (courseId) params.courseId = courseId;
      const res = await enrollmentService.getFacultyEnrollments(params);
      const pageData = res.data;
      setEnrollments(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load enrollments.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEnrollments(0, selectedCourseId);
  }, [selectedCourseId]);

  const columns = [
    { header: 'Roll Number', accessor: 'studentRollNumber', cellClassName: 'font-mono text-xs font-semibold' },
    { header: 'Student Name', accessor: 'studentName' },
    { header: 'Course Code', accessor: 'courseCode' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Academic Year', accessor: 'academicYear' },
    { header: 'Semester', accessor: 'semester', align: 'center' },
    { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status} /> },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Course Enrollments"
        subtitle="Roster of enrolled students across your assigned courses"
      >
        <div className="w-64">
          <Select
            placeholder="All Assigned Courses"
            value={selectedCourseId}
            onChange={(e) => setSelectedCourseId(e.target.value)}
            options={courses.map((c) => ({ value: String(c.courseId), label: `${c.courseCode} - ${c.courseName}` }))}
          />
        </div>
      </PageHeader>

      <Table
        columns={columns}
        data={enrollments}
        loading={loading}
        error={error}
        onRetry={() => fetchEnrollments(page)}
        emptyMessage="No enrolled students found for the selected criteria."
        keyField="enrollmentId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={20}
        onPageChange={(newPage) => fetchEnrollments(newPage)}
      />
    </div>
  );
};

export default FacultyEnrollmentsPage;
