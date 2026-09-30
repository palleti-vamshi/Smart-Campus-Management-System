import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { dashboardService } from '../../services/dashboardService';
import { useTheme } from '../../context/ThemeContext';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import { StatCardSkeleton, ChartSkeleton, TableSkeleton } from '../../components/common/Skeleton';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge, { CourseTypeBadge } from '../../components/common/Badge';
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
  FiUser,
  FiArrowRight,
  FiMapPin,
  FiCoffee,
  FiAlertCircle,
  FiCheckCircle,
} from 'react-icons/fi';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, Cell } from 'recharts';
import {
  ACADEMIC_CALENDAR_2026_27,
  getHolidayInfo,
  isLunchBreak,
  isSlotCurrent,
  formatProgramName,
  getAuthoritativeClassroom,
  formatDateDDMMYYYY,
  getCurrentOrNextAssessment,
  formatMarks,
} from '../../utils/academicCalendar';

export const StudentDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { currentTheme } = useTheme();
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
    return (
      <div className="space-y-6">
        <PageHeader
          title="Good morning, Student"
          subtitle="Here's your academic overview."
        />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <StatCardSkeleton key={i} />
          ))}
        </div>
        <ChartSkeleton height="h-64" />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <TableSkeleton rows={4} cols={3} />
          <TableSkeleton rows={4} cols={3} />
        </div>
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error} onRetry={fetchDashboard} />;
  }

  const p = data?.profile || {};
  const q = data?.quickSummary || {};
  const firstName = p.firstName || (p.fullName ? p.fullName.split(' ')[0] : 'Student');

  const courseAttendance = (data?.attendance?.byCourse || []).map((c) => {
    const match = c.courseName ? c.courseName.match(/\(([^)]+)\)/) : null;
    const shortLabel = match ? match[1] : c.courseCode;

    return {
      name: shortLabel,
      courseCode: c.courseCode,
      courseName: c.courseName,
      courseType: c.courseType || 'THEORY',
      attendance: c.attendancePercentage != null ? c.attendancePercentage : (c.percentage || 0),
      presentClasses: c.presentClasses ?? c.presentCount ?? 0,
      totalClasses: c.totalClasses ?? 0,
      absentClasses: c.absentClasses ?? c.absentCount ?? 0,
    };
  });

  const upcomingExams = data?.upcomingExams || [];
  const todayClasses = data?.timetable?.todaySchedule || data?.timetable?.todayClasses || [];
  const recentMarks = data?.marks?.marks || [];
  const chartColors = currentTheme.chartColors || ['#720921', '#B82A45', '#C07D45', '#4A6B56', '#6B5876'];

  const holidayInfo = getHolidayInfo();
  const inLunch = isLunchBreak();
  const currentOrNextAssessment = getCurrentOrNextAssessment();
  const authoritativeRoom = getAuthoritativeClassroom(p.programCode || p.programName, p.section || 'A', p.rollNumber);

  const marksCols = [
    {
      header: 'Course',
      render: (r) => (
        <div>
          <span className="font-semibold text-theme-primary block text-sm">{r.courseName}</span>
          <span className="font-mono text-xs text-theme-brand font-semibold">{r.courseCode}</span>
        </div>
      ),
    },
    {
      header: 'Assessment Type',
      align: 'center',
      render: (r) => {
        const isLab =
          r.courseType === 'LAB' ||
          r.courseType === 'LABORATORY' ||
          r.examType === 'LAB' ||
          (r.examName && r.examName.toLowerCase().includes('practical')) ||
          (r.courseName && r.courseName.toUpperCase().includes('LAB'));
        return <CourseTypeBadge label={isLab ? 'PRACTICAL' : 'THEORY'} />;
      },
    },
    { header: 'Assessment', accessor: 'examName', cellClassName: 'font-medium text-theme-primary text-xs' },
    {
      header: 'Score',
      render: (r) => (
        <span className="font-mono font-bold text-theme-primary">
          {formatMarks(r.marksObtained)} <span className="text-theme-muted font-normal text-xs">/ {formatMarks(r.maxMarks)}</span>
        </span>
      ),
      align: 'right',
    },
    {
      header: 'Grade',
      accessor: 'grade',
      align: 'center',
      render: (r) => (
        <span className="font-bold text-xs px-2 py-0.5 rounded bg-theme-elevated text-theme-brand border border-theme font-mono">
          {r.grade || '-'}
        </span>
      ),
    },
  ];

  const timetableCols = [
    {
      header: 'Time Slot',
      render: (r) => {
        const isCurrent = isSlotCurrent(r.startTime, r.endTime);
        const startStr = typeof r.startTime === 'string' ? r.startTime.slice(0, 5) : r.startTime;
        const endStr = typeof r.endTime === 'string' ? r.endTime.slice(0, 5) : r.endTime;
        return (
          <div className="flex items-center gap-1.5">
            <span className="font-mono text-xs font-semibold text-theme-primary">{startStr} - {endStr}</span>
            {isCurrent && (
              <span className="flex items-center gap-1 text-[10px] font-bold text-white bg-emerald-600 px-2 py-0.5 rounded-full border border-emerald-700 shadow-xs">
                <span className="h-1.5 w-1.5 rounded-full bg-white animate-ping"></span>
                LIVE
              </span>
            )}
          </div>
        );
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
    { header: 'Faculty', accessor: 'facultyName' },
    {
      header: 'Room',
      render: (r) => (
        <span className="font-bold text-xs px-2.5 py-1 rounded bg-theme-elevated text-theme-primary border border-theme">
          {r.roomNumber || r.classroom || authoritativeRoom}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* 1. Personalized Header */}
      <div>
        <h1 className="text-2xl font-extrabold tracking-tight text-theme-primary">
          Good morning, {firstName}
        </h1>
        <p className="text-sm text-theme-secondary mt-1">
          Here's your academic overview for {ACADEMIC_CALENDAR_2026_27.SEMESTER}.
        </p>
      </div>

      {/* 2. Student Profile Mini-Card with Section & Authoritative Room */}
      <div className="bg-theme-surface rounded-2xl p-5 sm:p-6 border border-theme shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div
            className="w-14 h-14 rounded-2xl flex items-center justify-center font-extrabold text-xl text-white shadow-md border border-white/20 shrink-0"
            style={{ backgroundColor: 'var(--color-primary)' }}
          >
            {firstName.charAt(0)}
          </div>
          <div>
            <div className="flex items-center gap-2.5 flex-wrap">
              <h2 className="text-lg font-bold text-theme-primary tracking-tight">
                {p.fullName || `${p.firstName || ''} ${p.lastName || ''}`}
              </h2>
              <span
                className="px-2.5 py-0.5 rounded-full text-xs font-bold border border-theme font-mono"
                style={{
                  backgroundColor: 'var(--color-primary-light)',
                  color: 'var(--color-primary)',
                }}
              >
                {p.rollNumber}
              </span>
              <span className="px-2 py-0.5 rounded-md text-xs font-bold bg-theme-elevated text-theme-primary border border-theme font-mono">
                SEC {p.section || 'A'}
              </span>
              <span className="px-2 py-0.5 rounded-md text-xs font-bold bg-theme-elevated text-theme-brand border border-theme flex items-center gap-1 font-mono">
                <FiMapPin className="text-[11px]" />
                Room {authoritativeRoom}
              </span>
            </div>
            <div className="flex items-center gap-4 mt-1.5 text-xs text-theme-secondary flex-wrap">
              <span><b>Program:</b> {formatProgramName(p.programCode || p.programName)}</span>
              <span>•</span>
              <span><b>Semester:</b> III (B.Tech II Year)</span>
              <span>•</span>
              <span><b>Regulation:</b> R25</span>
              <span>•</span>
              <span><b>Admission Year:</b> {p.admissionYear || 2025}</span>
              <span>•</span>
              <span><b>Academic Year:</b> 2026–2027</span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2.5 shrink-0">
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate('/student/profile')}
            icon={FiUser}
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

      {/* 3. KPI Cards per spec */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          title="Attendance"
          value={q.overallAttendancePercentage != null ? `${q.overallAttendancePercentage.toFixed(1)}%` : 'N/A'}
          subtitle={q.overallAttendancePercentage >= 75 ? 'Above 75% threshold' : 'Attention required'}
          icon={FiCheckSquare}
          color={q.overallAttendancePercentage >= 75 ? 'emerald' : 'rose'}
        />
        <StatCard
          title="Courses"
          value={q.enrolledCoursesCount ?? 0}
          subtitle="Enrolled this semester"
          icon={FiBook}
          color="primary"
        />
        <StatCard
          title="Upcoming Exams"
          value={q.upcomingExamsCount ?? 0}
          subtitle="Scheduled assessments"
          icon={FiCalendar}
          color="amber"
        />
        <StatCard
          title="Pending Documents"
          value={q.pendingDocumentRequests ?? 0}
          subtitle="Applications under review"
          icon={FiFileText}
          color="indigo"
        />
      </div>

      {/* 4. Course Attendance Visualization */}
      <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
          <div>
            <h2 className="text-base font-bold text-theme-primary">Course-wise Attendance (%)</h2>
            <p className="text-xs text-theme-muted mt-0.5">Subject compliance tracker for all enrolled courses</p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex items-center gap-3 text-xs font-semibold">
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: chartColors[0] || '#720921' }}></span>
                <span className="text-theme-secondary text-[11px]">Theory (≥75%)</span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-600"></span>
                <span className="text-theme-secondary text-[11px]">Laboratory (≥75%)</span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-rose-600"></span>
                <span className="text-theme-secondary text-[11px]">&lt;75% Risk</span>
              </div>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/student/attendance')}
              className="text-theme-brand font-semibold text-xs"
            >
              Detailed Records &rarr;
            </Button>
          </div>
        </div>
        <div className="h-72 overflow-x-auto">
          {courseAttendance.length > 0 ? (
            <div className="h-full min-w-[620px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={courseAttendance} margin={{ top: 10, right: 10, left: -20, bottom: 40 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(150,150,150,0.15)" />
                  <XAxis
                    dataKey="name"
                    interval={0}
                    angle={-30}
                    textAnchor="end"
                    height={55}
                    tick={{ fontSize: 11, fill: 'var(--text-secondary)', fontWeight: 600 }}
                  />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 12, fill: 'var(--text-secondary)' }} />
                  <Tooltip
                    content={({ active, payload }) => {
                      if (active && payload && payload.length) {
                        const item = payload[0].payload;
                        const isLab = item.courseType === 'LAB' || item.courseType === 'LABORATORY';
                        return (
                          <div className="bg-theme-surface p-3 rounded-xl border border-theme shadow-xl text-xs space-y-1.5 min-w-[220px]">
                            <div className="font-bold text-theme-primary text-sm leading-snug">{item.courseName}</div>
                            <div className="flex items-center justify-between gap-2 pt-1 border-t border-theme">
                              <span className="font-mono text-theme-brand font-bold text-xs">{item.courseCode}</span>
                              <CourseTypeBadge label={isLab ? 'LABORATORY' : 'THEORY'} />
                            </div>
                            <div className="flex items-center justify-between gap-2 text-xs">
                              <span className="text-theme-secondary font-medium">Attendance:</span>
                              <span
                                className={`font-extrabold text-sm ${
                                  item.attendance >= 75
                                    ? 'text-emerald-600 dark:text-emerald-400'
                                    : 'text-rose-600 dark:text-rose-400'
                                }`}
                              >
                                {Number(item.attendance).toFixed(1)}%
                              </span>
                            </div>
                            <div className="flex items-center justify-between gap-2 text-xs">
                              <span className="text-theme-secondary font-medium">Present / Total:</span>
                              <span className="font-bold text-theme-primary font-mono">
                                {item.presentClasses ?? 0} / {item.totalClasses ?? 0}
                              </span>
                            </div>
                          </div>
                        );
                      }
                      return null;
                    }}
                  />
                  <Bar dataKey="attendance" radius={[6, 6, 0, 0]} minPointSize={4} name="Attendance">
                    {courseAttendance.map((entry, index) => {
                      const isLab = entry.courseType === 'LAB' || entry.courseType === 'LABORATORY';
                      let color = chartColors[0] || '#720921';
                      if (entry.attendance < 75) {
                        color = '#DC2626';
                      } else if (isLab) {
                        color = '#059669';
                      }
                      return <Cell key={`cell-${index}`} fill={color} />;
                    })}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="h-full flex items-center justify-center text-theme-muted text-sm">
              No course attendance records found
            </div>
          )}
        </div>
      </div>

      {/* 5. Upcoming Exams & Recent Marks */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4 pb-2 border-b border-theme">
              <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
                <FiCalendar className="text-theme-brand" />
                <span>Upcoming / Current Assessment</span>
              </h2>
              <span className="text-[11px] font-mono font-semibold text-theme-secondary">
                Next Assessment
              </span>
            </div>

            <div className="p-4 rounded-xl bg-theme-elevated border border-theme space-y-3">
              <div className="flex items-center justify-between gap-2">
                <CourseTypeBadge label={currentOrNextAssessment.type || 'THEORY'} />
                <StatusBadge status={currentOrNextAssessment.status || 'IN PROGRESS'} />
              </div>

              <div>
                <h3 className="text-lg font-black text-theme-primary tracking-tight">
                  {currentOrNextAssessment.milestone || 'Sessional Examination – II'}
                </h3>
                <p className="text-xs text-theme-secondary font-medium mt-0.5">
                  B.Tech II Year – Semester III • Maximum Marks: {formatMarks(currentOrNextAssessment.maxMarks || 30)}
                </p>
              </div>

              <div className="flex items-center gap-2 pt-2 border-t border-theme-subtle">
                <FiClock className="w-4 h-4 text-theme-brand shrink-0" />
                <span className="font-mono text-xs font-bold text-theme-primary">
                  {currentOrNextAssessment.startDate} – {currentOrNextAssessment.endDate}
                </span>
                <span className="text-[11px] text-theme-muted font-medium ml-auto">
                  Official Academic Window
                </span>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-theme flex items-center justify-between">
            <span className="text-xs text-theme-secondary">
              Semester III Examination Schedule
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate('/student/exams')}
              className="font-bold text-xs"
            >
              View Full Schedule &rarr;
            </Button>
          </div>
        </div>

        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiAward className="text-theme-brand" />
              <span>Recent Marks & Results</span>
            </h2>
            <Button
              variant="ghost"
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

      {/* 6. Today's Classes & Department Notices */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiClock className="text-theme-brand" />
              <span>Today's Schedule</span>
            </h2>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/student/timetable')}
            >
              Full Timetable
            </Button>
          </div>

          {holidayInfo.isHoliday && (
            <div className="p-3.5 rounded-xl bg-theme-elevated border border-theme text-theme-primary mb-3 flex items-start gap-3 shadow-xs">
              <div className="p-1 rounded-md bg-amber-500/10 text-amber-600 dark:text-amber-400 mt-0.5 shrink-0">
                <FiAlertCircle className="w-5 h-5 text-amber-600 dark:text-amber-400" />
              </div>
              <div>
                <h4 className="font-bold text-xs uppercase tracking-wider text-theme-primary">Official Holiday — {holidayInfo.reason}</h4>
                <p className="text-xs text-theme-secondary font-medium mt-0.5">
                  No classes scheduled per the B.Tech II Year Academic Calendar. Regular instruction resumes on the next working day.
                </p>
              </div>
            </div>
          )}

          {inLunch && (
            <div className="p-3 rounded-xl bg-theme-elevated border border-theme text-theme-primary mb-3 flex items-center justify-between shadow-xs">
              <div className="flex items-center gap-2">
                <div className="p-1 rounded-md bg-amber-500/10 text-amber-600 dark:text-amber-400">
                  <FiCoffee className="w-4 h-4 text-amber-600 dark:text-amber-400" />
                </div>
                <span className="font-bold text-xs uppercase tracking-wider text-theme-primary">Lunch Break Active</span>
                <span className="text-xs text-theme-secondary font-medium">• 1:00 PM – 1:40 PM</span>
              </div>
              <span className="text-xs font-bold text-amber-600 dark:text-amber-400">Resumes at 1:40 PM</span>
            </div>
          )}

          <Table
            columns={timetableCols}
            data={todayClasses}
            emptyMessage={holidayInfo.isHoliday ? `No classes scheduled today (${holidayInfo.reason})` : "No classes scheduled for today"}
            keyField="timetableId"
          />
        </div>

        {/* Notices */}
        <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiBell className="text-amber-500" />
              <span>Department Notices</span>
            </h2>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/student/notices')}
            >
              Notice Board
            </Button>
          </div>
          {data?.notices?.recentNotices?.length > 0 ? (
            <div className="divide-y divide-theme-subtle">
              {data.notices.recentNotices.slice(0, 3).map((n) => (
                <div key={n.noticeId} className="py-2.5">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-semibold text-theme-primary text-sm">{n.title}</span>
                    <StatusBadge status={n.category} />
                    <StatusBadge status={n.priority} />
                  </div>
                  <p className="text-xs text-theme-secondary line-clamp-1">{n.content}</p>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-theme-muted py-6 text-center">No active notices</p>
          )}
        </div>
      </div>

      {/* 7. Official Academic Calendar Milestones (Semester III, 2026–2027) */}
      <div className="bg-theme-surface p-5 rounded-xl border border-theme shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
          <div>
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiCalendar className="text-theme-brand" />
              <span>Official Academic Calendar — {ACADEMIC_CALENDAR_2026_27.SEMESTER}</span>
            </h2>
            <p className="text-xs text-theme-secondary mt-0.5">
              {ACADEMIC_CALENDAR_2026_27.INSTITUTE} • {ACADEMIC_CALENDAR_2026_27.PROGRAM}
            </p>
          </div>
          <span className="text-xs font-mono font-semibold px-2.5 py-1 rounded bg-theme-elevated text-theme-primary border border-theme self-start sm:self-auto">
            Academic Year 2026–27
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
          {ACADEMIC_CALENDAR_2026_27.MILESTONES.map((m, idx) => (
            <div
              key={idx}
              className={`p-3.5 rounded-xl border transition-all ${
                m.isExam
                  ? 'bg-rose-50/50 dark:bg-rose-950/20 border-rose-200 dark:border-rose-900/40'
                  : 'bg-theme-elevated/40 border-theme'
              }`}
            >
              <div className="flex items-center justify-between gap-1 mb-1.5">
                <span className={`text-[11px] font-bold uppercase tracking-wider ${
                  m.isExam ? 'text-rose-700 dark:text-rose-400' : 'text-theme-brand'
                }`}>
                  {m.duration}
                </span>
                {m.isExam && (
                  <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-rose-100 dark:bg-rose-900/60 text-rose-700 dark:text-rose-300">
                    EXAM
                  </span>
                )}
              </div>
              <h4 className="font-semibold text-xs text-theme-primary leading-tight line-clamp-2">
                {m.milestone}
              </h4>
              <div className="text-[11px] text-theme-secondary mt-2 font-mono flex items-center justify-between border-t border-theme-subtle pt-1.5">
                <span>{m.startDate}</span>
                {m.endDate && m.endDate !== m.startDate && (
                  <>
                    <span>→</span>
                    <span>{m.endDate}</span>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default StudentDashboardPage;
