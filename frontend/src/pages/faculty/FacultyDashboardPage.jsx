import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { dashboardService } from '../../services/dashboardService';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/Badge';
import Table from '../../components/common/Table';
import Button from '../../components/common/Button';
import {
  FiBook,
  FiUsers,
  FiCheckSquare,
  FiCalendar,
  FiAward,
  FiClock,
  FiBell,
  FiArrowRight,
} from 'react-icons/fi';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

export const FacultyDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await dashboardService.getFacultyDashboard();
      setData(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Unable to load faculty dashboard.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  if (loading) {
    return <LoadingSpinner message="Loading faculty portal..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={fetchDashboard} />;
  }

  const courseAttendance = (data?.attendanceOverview?.byCourse || []).map((c) => ({
    name: c.courseCode,
    attendance: c.percentage || 0,
  }));

  const upcomingExams = data?.examSummary?.upcomingExams || [];
  const todayClasses = data?.timetableSummary?.todayClasses || [];

  const examCols = [
    { header: 'Course', accessor: 'courseName' },
    { header: 'Exam Type', accessor: 'examType', render: (row) => <StatusBadge status={row.examType} /> },
    { header: 'Date', accessor: 'examDate' },
    { header: 'Max Marks', accessor: 'maxMarks', align: 'right' },
  ];

  const timetableCols = [
    { header: 'Time Slot', render: (r) => `${r.startTime} - ${r.endTime}` },
    { header: 'Course', accessor: 'courseName' },
    { header: 'Classroom', accessor: 'classroom' },
    { header: 'Section', accessor: 'section' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome, ${data?.facultyName || 'Professor'}`}
        subtitle={`${data?.designation || 'Faculty'} · ${data?.departmentName || 'Department'} (${data?.employeeCode || ''})`}
      >
        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate('/faculty/attendance')}
          icon={FiCheckSquare}
        >
          Mark Attendance
        </Button>
        <Button
          variant="primary"
          size="sm"
          onClick={() => navigate('/faculty/marks')}
          icon={FiAward}
        >
          Enter Marks
        </Button>
      </PageHeader>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          title="Assigned Courses"
          value={data?.totalAssignedCourses ?? 0}
          icon={FiBook}
          color="primary"
        />
        <StatCard
          title="Enrolled Students"
          value={data?.studentEnrollmentSummary?.totalStudents ?? 0}
          icon={FiUsers}
          color="emerald"
        />
        <StatCard
          title="Avg Course Attendance"
          value={data?.attendanceOverview?.overallPercentage ? `${data.attendanceOverview.overallPercentage.toFixed(1)}%` : 'N/A'}
          icon={FiCheckSquare}
          color="indigo"
        />
        <StatCard
          title="Upcoming Exams"
          value={data?.examSummary?.totalUpcomingExams ?? 0}
          icon={FiCalendar}
          color="amber"
        />
      </div>

      {/* Assigned Courses & Attendance Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Assigned Courses */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-800 mb-4">Assigned Courses</h2>
            {data?.assignedCourses?.length > 0 ? (
              <div className="divide-y divide-slate-100">
                {data.assignedCourses.map((c) => (
                  <div key={c.courseId} className="py-2.5 flex items-center justify-between">
                    <div>
                      <p className="text-sm font-semibold text-slate-800">{c.courseName}</p>
                      <p className="text-xs text-slate-500">{c.courseCode} · Credits: {c.credits}</p>
                    </div>
                    <span className="text-xs px-2.5 py-1 bg-slate-100 text-slate-700 rounded-full font-medium">
                      Sem {c.semester}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-slate-400 py-6 text-center">No assigned courses found</p>
            )}
          </div>
          <div className="pt-4 border-t border-slate-100 mt-4 flex justify-end">
            <Button
              variant="text"
              size="sm"
              onClick={() => navigate('/faculty/courses')}
              className="text-primary-700"
            >
              View Course Details &rarr;
            </Button>
          </div>
        </div>

        {/* Course Attendance Chart */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-base font-bold text-slate-800 mb-4">Course Attendance Rate (%)</h2>
          <div className="h-64">
            {courseAttendance.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={courseAttendance} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
                  <Tooltip
                    contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0' }}
                    formatter={(val) => [`${val}%`, 'Attendance']}
                  />
                  <Bar dataKey="attendance" fill="#4f46e5" radius={[4, 4, 0, 0]} name="Attendance" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 text-sm">
                No attendance recorded yet
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Today's Classes & Upcoming Exams */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
              <FiClock className="text-primary-600" />
              <span>Today's Classes</span>
            </h2>
            <Button
              variant="text"
              size="sm"
              onClick={() => navigate('/faculty/timetable')}
            >
              Full Schedule
            </Button>
          </div>
          <Table
            columns={timetableCols}
            data={todayClasses}
            emptyMessage="No classes scheduled for today"
            keyField="id"
          />
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
              <FiCalendar className="text-primary-600" />
              <span>Upcoming Course Exams</span>
            </h2>
            <Button
              variant="text"
              size="sm"
              onClick={() => navigate('/faculty/exams')}
            >
              All Exams
            </Button>
          </div>
          <Table
            columns={examCols}
            data={upcomingExams}
            emptyMessage="No upcoming exams scheduled"
            keyField="examId"
          />
        </div>
      </div>

      {/* Notices */}
      {data?.noticeSummary?.recentNotices?.length > 0 && (
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
              <FiBell className="text-amber-500" />
              <span>Department Notices</span>
            </h2>
            <Button
              variant="text"
              size="sm"
              onClick={() => navigate('/faculty/notices')}
            >
              View All Notices
            </Button>
          </div>
          <div className="divide-y divide-slate-100">
            {data.noticeSummary.recentNotices.slice(0, 3).map((n) => (
              <div key={n.noticeId} className="py-3 flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-semibold text-slate-800 text-sm">{n.title}</span>
                    <StatusBadge status={n.category} />
                    <StatusBadge status={n.priority} />
                  </div>
                  <p className="text-xs text-slate-500 line-clamp-1">{n.content}</p>
                </div>
                <span className="text-xs text-slate-400 shrink-0">{n.publishedAt || n.publishDate}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default FacultyDashboardPage;
