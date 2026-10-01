import React, { useState, useEffect } from 'react';
import { enrollmentService } from '../../services/enrollmentService';
import { courseService } from '../../services/courseService';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Pagination from '../../components/common/Pagination';
import Select from '../../components/common/Select';
import StatusBadge, { CourseTypeBadge, SectionBadge } from '../../components/common/Badge';
import Button from '../../components/common/Button';
import { FiRefreshCw, FiUsers } from 'react-icons/fi';

export const FacultyEnrollmentsPage = () => {
  const [enrollments, setEnrollments] = useState([]);
  const [courses, setCourses] = useState([]);
  const [selectedCourseKey, setSelectedCourseKey] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const getCourseKey = (c) => `${c.courseId}___${c.section || 'A'}`;
  const parseCourseKey = (key) => {
    if (!key) return { courseId: null, section: null };
    const [cId, sec] = key.split('___');
    return { courseId: Number(cId), section: sec || null };
  };

  // Fetch faculty assigned courses for filter dropdown
  useEffect(() => {
    courseService.getFacultyCourses()
      .then((res) => {
        const list = res.data || [];
        setCourses(list);
      })
      .catch(() => {
        dashboardService.getFacultyDashboard()
          .then((res) => {
            setCourses(res.data?.assignedCourses || []);
          })
          .catch(() => {});
      });
  }, []);

  const fetchEnrollments = async (pageNumber = 0, courseKey = selectedCourseKey) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: pageNumber, size: 25 };
      const { courseId, section } = parseCourseKey(courseKey);
      if (courseId) params.courseId = courseId;
      if (section) params.section = section;
      const res = await enrollmentService.getFacultyEnrollments(params);
      const pageData = res.data;
      setEnrollments(pageData?.content || []);
      setPage(pageData?.pageNumber || 0);
      setTotalPages(pageData?.totalPages || 1);
      setTotalElements(pageData?.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load enrolled students.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEnrollments(0, selectedCourseKey);
  }, [selectedCourseKey]);

  const columns = [
    {
      header: 'Assigned Course',
      accessor: 'courseName',
      render: (r) => (
        <div>
          <span className="font-semibold text-theme-primary block text-sm">{r.courseName}</span>
          <span className="font-mono text-xs text-theme-secondary">{r.courseCode}</span>
        </div>
      ),
    },
    {
      header: 'Course Type',
      align: 'center',
      render: (r) => <CourseTypeBadge type={r.courseType} />,
    },
    {
      header: 'Program',
      accessor: 'programName',
      render: (r) => (
        <span className="text-xs font-semibold px-2 py-0.5 rounded bg-theme-elevated text-theme-primary border border-theme">
          {r.programCode || r.programName || 'AIML'}
        </span>
      ),
    },
    {
      header: 'Section',
      accessor: 'section',
      align: 'center',
      render: (r) => <SectionBadge section={r.section} />,
    },
    {
      header: 'Student Roll Number',
      accessor: 'studentRollNumber',
      cellClassName: 'font-mono text-xs font-bold text-theme-primary',
    },
    {
      header: 'Student Name',
      accessor: 'studentName',
      cellClassName: 'font-semibold text-theme-primary',
    },
    {
      header: 'Enrollment Status',
      accessor: 'status',
      align: 'center',
      render: (r) => <StatusBadge status={r.status} />,
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Course Enrollments"
        subtitle="Roster of enrolled students across your assigned teaching courses & sections"
      >
        <div className="flex items-center gap-3">
          <div className="w-80">
            <Select
              placeholder="All Assigned Courses & Sections"
              value={selectedCourseKey}
              onChange={(e) => setSelectedCourseKey(e.target.value)}
              options={[
                { value: '', label: 'All Assigned Courses & Sections' },
                ...courses.map((c) => {
                  const isLab = (c.courseType && String(c.courseType).toUpperCase().includes('LAB')) || (c.courseName && c.courseName.toUpperCase().includes('LAB'));
                  const typeLabel = isLab ? 'LABORATORY' : 'THEORY';
                  const sec = c.section || 'A';
                  return {
                    value: getCourseKey(c),
                    label: `${c.courseName} [Section ${sec}] (${typeLabel})`,
                  };
                }),
              ]}
            />
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => fetchEnrollments(page, selectedCourseKey)}
            icon={FiRefreshCw}
          >
            Refresh
          </Button>
        </div>
      </PageHeader>

      <Table
        columns={columns}
        data={enrollments}
        loading={loading}
        error={error}
        onRetry={() => fetchEnrollments(page, selectedCourseKey)}
        emptyMessage="No enrolled students found for the selected course and section."
        keyField="enrollmentId"
      />

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={25}
        onPageChange={(newPage) => fetchEnrollments(newPage, selectedCourseKey)}
      />
    </div>
  );
};

export default FacultyEnrollmentsPage;
