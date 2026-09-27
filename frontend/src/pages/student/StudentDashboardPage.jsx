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
  FiCheckSquare,
  FiBook,
  FiCalendar,
  FiFileText,
  FiBell,
  FiClock,
  FiAward,
} from 'react-icons/fi';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

export const StudentDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await dashboardService.getStudentDashboard();
      setData(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Unable to load student dashboard.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  if (loading) {
    return <LoadingSpinner message="Loading student portal..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={fetchDashboard} />;
  }

  const p = data?.profile || {};
  const q = data?.quickSummary || {};

  const courseAttendance = (data?.attendance?.byCourse || []).map((c) => ({
    name: c.courseCode,
    attendance: c.percentage || 0,
  }));

  const upcomingExams = data?.upcomingExams || [];
  const todayClasses = data?.timetable?.todayClasses || [];
  const recentMarks = data?.marks?.marks || [];

  const examCols = [
    { header: 'Course', accessor: 'courseName' },
    { header: 'Exam Type', accessor: 'examType', render: (row) => <StatusBadge status={row.examType} /> },
    { header: 'Date', accessor: 'examDate' },
    { header: 'Max Marks', accessor: 'maxMarks', align: 'right' },
  ];

  const marksCols = [
    { header: 'Course', accessor: 'courseName' },
    { header: 'Exam', accessor: 'examName' },
    { header: 'Marks Obtained', render: (r) => `${r.marksObtained} / ${r.maxMarks}`, align: 'right' },
    { header: 'Grade', accessor: 'grade', align: 'center', render: (r) => <span className="font-bold text-slate-800">{r.grade || '-'}</span> },
  ];

  const timetableCols = [
    { header: 'Time Slot', render: (r) => `${r.startTime} - ${r.endTime}` },
    { header: 'Course', accessor: 'courseName' },
    { header: 'Faculty', accessor: 'facultyName' },
    { header: 'Room', accessor: 'classroom' },
  ];

  return (
    <div className="space-y-6">
      {/* 1. Welcome / Student Profile Banner */}
      <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              {p.fullName || `${p.firstName || ''} ${p.lastName || ''}`}
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary-100 text-primary-800">
              {p.rollNumber}
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            {p.programName} ({p.programCode}) · Semester {p.currentSemester} · Section {p.section}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate('/student/profile')}
          >
            Full Profile
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => navigate('/student/documents')}
            icon={FiFileText}
          >
            Request Certificate
          </Button>
        </div>
      </div>

      {/* 2. KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        <StatCard
          title="Attendance"
          value={q.overallAttendancePercentage != null ? `${q.overallAttendancePercentage.toFixed(1)}%` : 'N/A'}
          icon={FiCheckSquare}
          color={q.overallAttendancePercentage >= 75 ? 'emerald' : 'rose'}
        />
        <StatCard
          title="Courses"
          value={q.enrolledCoursesCount ?? 0}
          icon={FiBook}
          color="primary"
        />
        <StatCard
          title="Upcoming Exams"
          value={q.upcomingExamsCount ?? 0}
          icon={FiCalendar}
          color="amber"
        />
        <StatCard
          title="Pending Docs"
          value={q.pendingDocumentRequests ?? 0}
          icon={FiFileText}
          color="indigo"
        />
        <StatCard
          title="Active Notices"
          value={q.activeNoticesCount ?? 0}
          icon={FiBell}
          color="rose"
        />
      </div>

      {/* 3. Course Attendance Chart */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-bold text-slate-800">Course-wise Attendance (%)</h2>
          <Button
            variant="text"
            size="sm"
            onClick={() => navigate('/student/attendance')}
          >
            Detailed Records &rarr;
          </Button>
        </div>
        <div className="h-60">
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
                <Bar dataKey="attendance" fill="#059669" radius={[4, 4, 0, 0]} name="Attendance" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex items-center justify-center text-slate-400 text-sm">
              No course attendance records found
            </div>
          )}
        </div>
      </div>

      {/* 4. Upcoming Exams & Marks */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
              <FiCalendar className="text-primary-600" />
              <span>Upcoming Examinations</span>
            </h2>
            <Button
              variant="text"
              size="sm"
              onClick={() => navigate('/student/exams')}
            >
              View Schedule
            </Button>
          </div>
          <Table
            columns={examCols}
            data={upcomingExams}
            emptyMessage="No exams scheduled at this time"
            keyField="examId"
          />
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
              <FiAward className="text-primary-600" />
              <span>Recent Marks & Results</span>
            </h2>
            <Button
              variant="text"
              size="sm"
              onClick={() => navigate('/student/marks')}
            >
              All Results
            </Button>
          </div>
          <Table
            columns={marksCols}
            data={recentMarks}
            emptyMessage="No marks published yet"
            keyField="markId"
          />
        </div>
      </div>

      {/* 5. Today's Classes & Notices */}
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
              onClick={() => navigate('/student/timetable')}
            >
              Full Timetable
            </Button>
          </div>
          <Table
            columns={timetableCols}
            data={todayClasses}
            emptyMessage="No classes scheduled for today"
            keyField="id"
          />
        </div>

        {/* Notices */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center gap-2">
              <FiBell className="text-amber-500" />
              <span>Department Notices</span>
            </h2>
            <Button
              variant="text"
              size="sm"
              onClick={() => navigate('/student/notices')}
            >
              Notice Board
            </Button>
          </div>
          {data?.notices?.recentNotices?.length > 0 ? (
            <div className="divide-y divide-slate-100">
              {data.notices.recentNotices.slice(0, 3).map((n) => (
                <div key={n.noticeId} className="py-2.5">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-semibold text-slate-800 text-sm">{n.title}</span>
                    <StatusBadge status={n.category} />
                    <StatusBadge status={n.priority} />
                  </div>
                  <p className="text-xs text-slate-500 line-clamp-1">{n.content}</p>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-400 py-6 text-center">No active notices</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default StudentDashboardPage;
