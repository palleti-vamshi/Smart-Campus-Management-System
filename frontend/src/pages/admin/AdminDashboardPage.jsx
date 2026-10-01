import React, { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { useTheme } from '../../context/ThemeContext';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import { StatCardSkeleton, ChartSkeleton, TableSkeleton } from '../../components/common/Skeleton';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/Badge';
import Table from '../../components/common/Table';
import {
  FiUsers,
  FiUserCheck,
  FiLayers,
  FiBook,
  FiFolder,
  FiCalendar,
  FiCheckCircle,
  FiClock,
  FiFileText,
  FiBell,
  FiTrendingUp,
} from 'react-icons/fi';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  Cell,
} from 'recharts';

export const AdminDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { currentTheme } = useTheme();

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await dashboardService.getAdminDashboard();
      setData(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Unable to load department dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="Department Overview"
          subtitle="Academic and operational snapshot"
        />
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <StatCardSkeleton key={i} />
          ))}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <ChartSkeleton height="h-64" />
          <ChartSkeleton height="h-64" />
        </div>
        <TableSkeleton rows={4} cols={4} />
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error} onRetry={fetchDashboard} />;
  }

  const dept = data?.departmentSummary || {};
  const studentsByProg = (data?.studentsByProgram || []).map((p) => ({
    name: p.programCode || p.programName,
    count: p.studentCount,
  }));

  const attendanceData = (data?.attendanceOverview?.byProgram || []).map((p) => ({
    name: p.programCode || p.programName,
    attendance: p.percentage || 0,
  }));

  const chartColors = currentTheme.chartColors || ['#720921', '#B82A45', '#C07D45', '#4A6B56', '#6B5876'];

  const examCols = [
    { header: 'Course', accessor: 'courseName' },
    {
      header: 'Exam Type',
      accessor: 'examType',
      render: (row) => <StatusBadge status={row.examType} />,
    },
    { header: 'Date', accessor: 'examDate' },
    {
      header: 'Max Marks',
      accessor: 'maxMarks',
      align: 'right',
      render: (row) => <span className="font-semibold text-theme-primary">{row.maxMarks}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Department Overview"
        subtitle="Academic and operational snapshot"
      />

      {/* 1. KPI Cards with icons & contextual labels */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <StatCard
          title="Total Students"
          value={dept.totalStudents ?? 0}
          subtitle="Enrolled in AIML/IOT/RAI"
          icon={FiUsers}
          color="primary"
        />
        <StatCard
          title="Faculty"
          value={dept.totalFaculty ?? 0}
          subtitle="Active academic staff"
          icon={FiUserCheck}
          color="emerald"
        />
        <StatCard
          title="Programs"
          value={dept.totalPrograms ?? 0}
          subtitle="UG Degree programs"
          icon={FiLayers}
          color="amber"
        />
        <StatCard
          title="Courses"
          value={dept.totalCourses ?? 0}
          subtitle="Curriculum offerings"
          icon={FiBook}
          color="indigo"
        />
        <StatCard
          title="Enrollments"
          value={data?.enrollmentSummary?.totalEnrollments ?? 0}
          subtitle="Active course enrollments"
          icon={FiCheckCircle}
          color="teal"
        />
        <StatCard
          title="Classrooms"
          value={dept.totalClassrooms ?? 0}
          subtitle="Lecture halls & labs"
          icon={FiFolder}
          color="rose"
        />
      </div>

      {/* 2. Charts: Students by Program & Program Attendance */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Student Distribution */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary">
              Students by Program
            </h2>
            <span className="text-xs text-theme-muted font-medium">Headcount distribution</span>
          </div>
          <div className="h-64">
            {studentsByProg.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={studentsByProg} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(150,150,150,0.15)" />
                  <XAxis dataKey="name" tick={{ fontSize: 12, fill: 'var(--text-secondary)' }} />
                  <YAxis tick={{ fontSize: 12, fill: 'var(--text-secondary)' }} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'var(--bg-surface)',
                      borderColor: 'var(--border-color)',
                      color: 'var(--text-primary)',
                      borderRadius: '8px',
                    }}
                  />
                  <Bar dataKey="count" radius={[6, 6, 0, 0]} name="Students">
                    {studentsByProg.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={chartColors[index % chartColors.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-theme-muted text-sm">
                No student program data available
              </div>
            )}
          </div>
        </div>

        {/* Attendance Overview */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-bold text-theme-primary">
                Attendance Overview
              </h2>
              <p className="text-xs text-theme-muted mt-0.5">Program-level compliance</p>
            </div>
            <span
              className="text-xs font-bold px-2.5 py-1 rounded-full border border-theme"
              style={{
                backgroundColor: 'var(--color-primary-light)',
                color: 'var(--color-primary)',
              }}
            >
              Avg: {data?.attendanceOverview?.overallPercentage ? `${data.attendanceOverview.overallPercentage.toFixed(1)}%` : 'N/A'}
            </span>
          </div>
          <div className="h-64">
            {attendanceData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={attendanceData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(150,150,150,0.15)" />
                  <XAxis dataKey="name" tick={{ fontSize: 12, fill: 'var(--text-secondary)' }} />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 12, fill: 'var(--text-secondary)' }} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'var(--bg-surface)',
                      borderColor: 'var(--border-color)',
                      color: 'var(--text-primary)',
                      borderRadius: '8px',
                    }}
                    formatter={(val) => [`${val}%`, 'Attendance']}
                  />
                  <Bar dataKey="attendance" fill={chartColors[1 % chartColors.length]} radius={[6, 6, 0, 0]} name="Attendance %" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-theme-muted text-sm">
                No attendance records found
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 3. Academic Performance, Active Notices & Document Requests */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Academic Performance */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-theme-primary mb-3 flex items-center gap-2">
              <FiTrendingUp className="text-theme-brand" />
              <span>Academic Performance</span>
            </h2>
            <div className="space-y-3 mt-4">
              <div className="flex justify-between items-center py-2 border-b border-theme-subtle text-sm">
                <span className="text-theme-secondary">Total Exams Conducted</span>
                <span className="font-bold text-theme-primary">{data?.academicPerformance?.examCount ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-theme-subtle text-sm">
                <span className="text-theme-secondary">Marks Entries</span>
                <span className="font-bold text-theme-primary">{data?.academicPerformance?.marksCount ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 text-sm">
                <span className="text-theme-secondary">Department Average Score</span>
                <span className="font-bold text-theme-brand">
                  {data?.academicPerformance?.averageMarks ? `${data.academicPerformance.averageMarks.toFixed(1)} / 100` : 'N/A'}
                </span>
              </div>
            </div>
          </div>
          {data?.academicPerformance?.coursePerformance?.length > 0 && (
            <div className="mt-4 pt-3 border-t border-theme-subtle">
              <p className="text-xs text-theme-muted uppercase font-semibold mb-2">Top Course Averages</p>
              <div className="space-y-1.5">
                {data.academicPerformance.coursePerformance.slice(0, 3).map((cp) => (
                  <div key={cp.courseCode} className="flex justify-between text-xs">
                    <span className="text-theme-secondary truncate">{cp.courseName}</span>
                    <span className="font-bold text-theme-primary">{cp.averageMarks?.toFixed(1)}%</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Notices Summary */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-theme-primary mb-3 flex items-center justify-between">
              <span>Active Notices</span>
              <FiBell className="text-theme-muted" />
            </h2>
            <div className="space-y-3 mt-4">
              <div className="flex justify-between items-center py-2 border-b border-theme-subtle text-sm">
                <span className="text-theme-secondary">Published Notices</span>
                <span className="font-bold text-theme-primary">{data?.noticeSummary?.totalActive ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-theme-subtle text-sm">
                <span className="text-theme-secondary">Urgent Priority</span>
                <span className="font-bold text-red-600 dark:text-red-400">{data?.noticeSummary?.urgentCount ?? 0}</span>
              </div>
            </div>
          </div>
          {data?.noticeSummary?.byCategory && (
            <div className="mt-4 pt-3 border-t border-theme-subtle">
              <p className="text-xs text-theme-muted uppercase font-semibold mb-2">Category Distribution</p>
              <div className="flex flex-wrap gap-1.5">
                {Object.entries(data.noticeSummary.byCategory).map(([cat, cnt]) => (
                  <span key={cat} className="text-xs px-2.5 py-1 bg-theme-elevated text-theme-primary border border-theme rounded-lg font-medium">
                    {cat}: <b>{cnt}</b>
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Document Requests Summary */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-theme-primary mb-3 flex items-center justify-between">
              <span>Document Requests</span>
              <FiFileText className="text-theme-muted" />
            </h2>
            <div className="space-y-3 mt-4">
              <div className="flex justify-between items-center py-2 border-b border-theme-subtle text-sm">
                <span className="text-theme-secondary">Pending Review</span>
                <span className="font-bold text-amber-600 dark:text-amber-400">{data?.documentSummary?.pendingRequests ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-theme-subtle text-sm">
                <span className="text-theme-secondary">Approved</span>
                <span className="font-bold text-blue-600 dark:text-blue-400">{data?.documentSummary?.approvedRequests ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 text-sm">
                <span className="text-theme-secondary">Issued</span>
                <span className="font-bold text-emerald-600 dark:text-emerald-400">{data?.documentSummary?.issuedRequests ?? 0}</span>
              </div>
            </div>
          </div>
          <div className="mt-4 pt-3 border-t border-theme-subtle text-xs text-theme-muted flex justify-between">
            <span>Rejected Requests:</span>
            <span className="font-bold text-theme-secondary">{data?.documentSummary?.rejectedRequests ?? 0}</span>
          </div>
        </div>
      </div>

      {/* 4. Upcoming Exams Table */}
      <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
        <h2 className="text-base font-bold text-theme-primary mb-4 flex items-center gap-2">
          <FiCalendar className="text-theme-brand" />
          <span>Upcoming Department Examinations</span>
        </h2>
        <Table
          columns={examCols}
          data={data?.upcomingExams || []}
          emptyMessage="No upcoming examinations scheduled"
          keyField="examId"
        />
      </div>
    </div>
  );
};

export default AdminDashboardPage;
