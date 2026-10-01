import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { dashboardService } from '../../services/dashboardService';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import { StatCardSkeleton, ChartSkeleton, TableSkeleton } from '../../components/common/Skeleton';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge, { CourseTypeBadge, SectionBadge } from '../../components/common/Badge';
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
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, Cell } from 'recharts';

export const FacultyDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { currentTheme } = useTheme();
  const { user } = useAuth();
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
    return (
      <div className="space-y-6">
        <PageHeader
          title="Good morning, Faculty"
          subtitle="Here's your teaching overview."
        />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <StatCardSkeleton key={i} />
          ))}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <ChartSkeleton height="h-64" />
          <TableSkeleton rows={4} cols={3} />
        </div>
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error} onRetry={fetchDashboard} />;
  }

  const courseAttendance = (data?.attendanceOverview?.byCourse || []).map((c) => ({
    name: c.courseCode,
    attendance: c.percentage != null ? c.percentage : (c.attendancePercentage != null ? c.attendancePercentage : 0),
  }));

  const upcomingExams = data?.examSummary?.upcomingExams || [];
  const todayClasses = data?.timetableSummary?.todaySchedule || data?.timetableSummary?.todayClasses || [];
  const chartColors = currentTheme.chartColors || ['#720921', '#B82A45', '#C07D45', '#4A6B56', '#6B5876'];

  const examCols = [
    { header: 'Course', accessor: 'courseName' },
    { header: 'Exam', accessor: 'examName' },
    { header: 'Type', accessor: 'examType', render: (row) => <StatusBadge status={row.examType} /> },
    { header: 'Date', accessor: 'examDate' },
    {
      header: 'Max Marks',
      accessor: 'maxMarks',
      align: 'right',
      render: (row) => <span className="font-semibold text-theme-primary">{row.maxMarks}</span>,
    },
  ];

  const timetableCols = [
    {
      header: 'Time Slot',
      render: (r) => {
        const startStr = typeof r.startTime === 'string' ? r.startTime.slice(0, 5) : r.startTime;
        const endStr = typeof r.endTime === 'string' ? r.endTime.slice(0, 5) : r.endTime;
        return <span className="font-mono text-xs font-semibold text-theme-primary">{startStr} - {endStr}</span>;
      },
    },
    {
      header: 'Course',
      render: (r) => (
        <div>
          <span className="font-semibold text-theme-primary block text-sm">{r.courseName}</span>
          <span className="font-mono text-xs text-theme-secondary">{r.courseCode}</span>
        </div>
      ),
    },
    {
      header: 'Room',
      render: (r) => (
        <span className="font-bold text-xs px-2 py-0.5 rounded bg-theme-elevated text-theme-primary border border-theme">
          {r.roomNumber || r.classroom || 'N/A'}
        </span>
      ),
    },
    {
      header: 'Section',
      render: (r) => <SectionBadge section={r.section} />,
    },
  ];

  const facultyGreetingName = data?.facultyName || user?.fullName || user?.username || 'Faculty';

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Good morning, ${facultyGreetingName}`}
        subtitle="Here's your teaching and academic overview."
      >
        <div className="flex items-center gap-3">
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
        </div>
      </PageHeader>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          title="Assigned Courses"
          value={data?.totalAssignedCourses ?? data?.assignedCourses?.length ?? 0}
          subtitle="Current semester"
          icon={FiBook}
          color="primary"
        />
        <StatCard
          title="Students"
          value={data?.studentEnrollmentSummary?.totalStudents ?? 0}
          subtitle="Enrolled across sections"
          icon={FiUsers}
          color="emerald"
        />
        <StatCard
          title="Attendance"
          value={data?.attendanceOverview?.overallPercentage ? `${data.attendanceOverview.overallPercentage.toFixed(1)}%` : 'N/A'}
          subtitle="Class average rate"
          icon={FiCheckSquare}
          color="indigo"
        />
        <StatCard
          title="Upcoming Exams"
          value={data?.examSummary?.totalUpcomingExams ?? upcomingExams.length ?? 0}
          subtitle="Scheduled this term"
          icon={FiCalendar}
          color="amber"
        />
      </div>

      {/* Assigned Courses & Attendance Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Assigned Courses List */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-base font-bold text-theme-primary">Assigned Courses</h2>
              <span className="text-xs text-theme-muted font-medium">{data?.assignedCourses?.length || 0} active</span>
            </div>
            {data?.assignedCourses?.length > 0 ? (
              <div className="divide-y divide-theme-subtle">
                {data.assignedCourses.map((c, idx) => (
                  <div key={`${c.courseId}_${c.section || idx}`} className="py-3 flex items-center justify-between">
                    <div>
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <p className="text-sm font-semibold text-theme-primary">{c.courseName}</p>
                        <CourseTypeBadge type={c.courseType} />
                        {c.section && <SectionBadge section={c.section} />}
                      </div>
                      <p className="text-xs text-theme-secondary flex items-center gap-2 font-mono">
                        <span className="font-bold text-theme-primary">{c.courseCode}</span>
                        <span>· Credits: {c.credits}</span>
                        {c.enrolledStudents != null && (
                          <span className="text-emerald-700 dark:text-emerald-400 font-semibold font-sans">
                            · {c.enrolledStudents} students
                          </span>
                        )}
                      </p>
                    </div>
                    <span className="text-xs px-2.5 py-1 bg-theme-elevated text-theme-primary border border-theme rounded-full font-semibold shrink-0">
                      Sem {c.semester}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-theme-muted py-6 text-center">No assigned courses found</p>
            )}
          </div>
          <div className="pt-4 border-t border-theme-subtle mt-4 flex justify-end">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/faculty/courses')}
              className="text-theme-brand font-semibold"
            >
              View Course Details &rarr;
            </Button>
          </div>
        </div>

        {/* Course Attendance Chart */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary">Course Attendance Rate (%)</h2>
            <span className="text-xs text-theme-muted">Batch compliance</span>
          </div>
          <div className="h-64">
            {courseAttendance.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={courseAttendance} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
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
                  <Bar dataKey="attendance" radius={[6, 6, 0, 0]} name="Attendance">
                    {courseAttendance.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={chartColors[index % chartColors.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-theme-muted text-sm">
                No attendance recorded yet
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Today's Classes & Upcoming Exams */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiClock className="text-theme-brand" />
              <span>Today's Classes</span>
            </h2>
            <Button
              variant="ghost"
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

        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiCalendar className="text-theme-brand" />
              <span>Upcoming Course Exams</span>
            </h2>
            <Button
              variant="ghost"
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

      {/* Department Notices */}
      {data?.noticeSummary?.recentNotices?.length > 0 && (
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiBell className="text-amber-500" />
              <span>Department Notices</span>
            </h2>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/faculty/notices')}
            >
              View All Notices
            </Button>
          </div>
          <div className="divide-y divide-theme-subtle">
            {data.noticeSummary.recentNotices.slice(0, 3).map((n) => (
              <div key={n.noticeId} className="py-3 flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-semibold text-theme-primary text-sm">{n.title}</span>
                    <StatusBadge status={n.category} />
                    <StatusBadge status={n.priority} />
                  </div>
                  <p className="text-xs text-theme-secondary line-clamp-1">{n.content}</p>
                </div>
                <span className="text-xs text-theme-muted shrink-0">{n.publishedAt || n.publishDate}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default FacultyDashboardPage;
