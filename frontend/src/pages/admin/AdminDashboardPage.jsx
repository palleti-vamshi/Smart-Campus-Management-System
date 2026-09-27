import React, { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
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
} from 'react-icons/fi';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';

const COLORS = ['#2563eb', '#0d9488', '#d97706', '#dc2626', '#8b5cf6'];

export const AdminDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await dashboardService.getAdminDashboard();
      setData(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Unable to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  if (loading) {
    return <LoadingSpinner message="Loading department overview..." />;
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

  const examCols = [
    { header: 'Course', accessor: 'courseName' },
    { header: 'Exam Type', accessor: 'examType', render: (row) => <StatusBadge status={row.examType} /> },
    { header: 'Date', accessor: 'examDate' },
    { header: 'Max Marks', accessor: 'maxMarks', align: 'right' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Department Overview"
        subtitle="Real-time academic performance, enrollment, and operations"
      />

      {/* 1. KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        <StatCard
          title="Total Students"
          value={dept.totalStudents ?? 0}
          icon={FiUsers}
          color="primary"
        />
        <StatCard
          title="Faculty Members"
          value={dept.totalFaculty ?? 0}
          icon={FiUserCheck}
          color="emerald"
        />
        <StatCard
          title="Programs"
          value={dept.totalPrograms ?? 0}
          icon={FiLayers}
          color="amber"
        />
        <StatCard
          title="Courses"
          value={dept.totalCourses ?? 0}
          icon={FiBook}
          color="indigo"
        />
        <StatCard
          title="Classrooms"
          value={dept.totalClassrooms ?? 0}
          icon={FiFolder}
          color="rose"
        />
      </div>

      {/* 2. Charts: Student Distribution & Attendance Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Student Distribution */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-base font-bold text-slate-800 mb-4">
            Students by Program
          </h2>
          <div className="h-64">
            {studentsByProg.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={studentsByProg} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip
                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0' }}
                  />
                  <Bar dataKey="count" fill="#2563eb" radius={[4, 4, 0, 0]} name="Students" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 text-sm">
                No student program data available
              </div>
            )}
          </div>
        </div>

        {/* Attendance by Program */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-800">
              Program Attendance (%)
            </h2>
            <span className="text-xs font-semibold px-2 py-1 bg-primary-50 text-primary-700 rounded">
              Avg: {data?.attendanceOverview?.overallPercentage ? `${data.attendanceOverview.overallPercentage.toFixed(1)}%` : 'N/A'}
            </span>
          </div>
          <div className="h-64">
            {attendanceData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={attendanceData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
                  <Tooltip
                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0' }}
                    formatter={(val) => [`${val}%`, 'Attendance']}
                  />
                  <Bar dataKey="attendance" fill="#0d9488" radius={[4, 4, 0, 0]} name="Attendance %" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 text-sm">
                No attendance records found
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 3. Academic Performance & Enrollment Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Academic Performance */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-800 mb-3">Academic Performance</h2>
            <div className="space-y-3 mt-4">
              <div className="flex justify-between items-center py-2 border-b border-slate-100 text-sm">
                <span className="text-slate-500">Total Exams Conducted</span>
                <span className="font-semibold text-slate-800">{data?.academicPerformance?.examCount ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-slate-100 text-sm">
                <span className="text-slate-500">Marks Entries</span>
                <span className="font-semibold text-slate-800">{data?.academicPerformance?.marksCount ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 text-sm">
                <span className="text-slate-500">Average Score</span>
                <span className="font-semibold text-primary-700">
                  {data?.academicPerformance?.averageMarks ? `${data.academicPerformance.averageMarks.toFixed(1)} / 100` : 'N/A'}
                </span>
              </div>
            </div>
          </div>
          {data?.academicPerformance?.coursePerformance?.length > 0 && (
            <div className="mt-4 pt-3 border-t border-slate-100">
              <p className="text-xs text-slate-400 uppercase font-semibold mb-2">Top Course Averages</p>
              <div className="space-y-1">
                {data.academicPerformance.coursePerformance.slice(0, 3).map((cp) => (
                  <div key={cp.courseCode} className="flex justify-between text-xs">
                    <span className="text-slate-600 truncate">{cp.courseName}</span>
                    <span className="font-semibold text-slate-800">{cp.averageMarks?.toFixed(1)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Notices Summary */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-800 mb-3 flex items-center justify-between">
              <span>Notice Center</span>
              <FiBell className="text-slate-400" />
            </h2>
            <div className="space-y-3 mt-4">
              <div className="flex justify-between items-center py-2 border-b border-slate-100 text-sm">
                <span className="text-slate-500">Active Notices</span>
                <span className="font-semibold text-slate-800">{data?.noticeSummary?.totalActive ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-slate-100 text-sm">
                <span className="text-slate-500">Urgent Priority</span>
                <span className="font-semibold text-red-600">{data?.noticeSummary?.urgentCount ?? 0}</span>
              </div>
            </div>
          </div>
          {data?.noticeSummary?.byCategory && (
            <div className="mt-4 pt-3 border-t border-slate-100">
              <p className="text-xs text-slate-400 uppercase font-semibold mb-2">Category Distribution</p>
              <div className="flex flex-wrap gap-1.5">
                {Object.entries(data.noticeSummary.byCategory).map(([cat, cnt]) => (
                  <span key={cat} className="text-xs px-2 py-0.5 bg-slate-100 text-slate-700 rounded-md">
                    {cat}: <b>{cnt}</b>
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Document Requests Summary */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-800 mb-3 flex items-center justify-between">
              <span>Digital Documents</span>
              <FiFileText className="text-slate-400" />
            </h2>
            <div className="space-y-3 mt-4">
              <div className="flex justify-between items-center py-2 border-b border-slate-100 text-sm">
                <span className="text-slate-500">Pending Review</span>
                <span className="font-semibold text-amber-600">{data?.documentSummary?.pendingRequests ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-slate-100 text-sm">
                <span className="text-slate-500">Approved</span>
                <span className="font-semibold text-blue-600">{data?.documentSummary?.approvedRequests ?? 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 text-sm">
                <span className="text-slate-500">Issued</span>
                <span className="font-semibold text-emerald-600">{data?.documentSummary?.issuedRequests ?? 0}</span>
              </div>
            </div>
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 text-xs text-slate-400 flex justify-between">
            <span>Rejected Requests:</span>
            <span className="font-semibold text-slate-600">{data?.documentSummary?.rejectedRequests ?? 0}</span>
          </div>
        </div>
      </div>

      {/* 4. Upcoming Exams Table */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <h2 className="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
          <FiCalendar className="text-primary-600" />
          <span>Upcoming Department Exams</span>
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
