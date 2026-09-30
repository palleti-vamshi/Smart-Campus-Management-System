import React, { useState, useEffect } from 'react';
import { attendanceService } from '../../services/attendanceService';
import { enrollmentService } from '../../services/enrollmentService';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import Table from '../../components/common/Table';
import StatusBadge, { CourseTypeBadge } from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import { FiCheckSquare, FiXCircle, FiClock, FiCheck, FiLayers } from 'react-icons/fi';
import { formatDateDDMMYYYY } from '../../utils/academicCalendar';

export const StudentAttendancePage = () => {
  const [summary, setSummary] = useState(null);
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [sumRes, recRes, enrRes] = await Promise.all([
        attendanceService.getMyAttendanceSummary(),
        attendanceService.getMyAttendance(),
        enrollmentService.getMyEnrollments(),
      ]);

      const rawSummaryList = sumRes?.data || (Array.isArray(sumRes) ? sumRes : []);
      const summaryList = Array.isArray(rawSummaryList) ? rawSummaryList : [];

      const rawEnrList = enrRes?.data?.content || enrRes?.content || enrRes?.data || (Array.isArray(enrRes) ? enrRes : []);
      const enrMap = new Map();
      (Array.isArray(rawEnrList) ? rawEnrList : []).forEach((e) => {
        if (e.courseId) enrMap.set(e.courseId, e);
        if (e.courseCode) enrMap.set(e.courseCode, e);
      });

      const mergedCourses = summaryList.map((s) => {
        const enr = enrMap.get(s.courseId) || enrMap.get(s.courseCode) || {};
        return {
          ...s,
          credits: enr.credits != null ? enr.credits : s.credits,
          facultyName: enr.facultyName || s.facultyName,
        };
      });

      const totalHeld = summaryList.reduce((acc, c) => acc + (c.totalClasses || 0), 0);
      const totalPresent = summaryList.reduce((acc, c) => acc + (c.presentCount || 0), 0);
      const totalAbsent = summaryList.reduce((acc, c) => acc + (c.absentCount || 0), 0);
      const overallPercentage = totalHeld > 0 ? (totalPresent / totalHeld) * 100 : 0;

      setSummary({
        totalClasses: totalHeld,
        presentClasses: totalPresent,
        absentClasses: totalAbsent,
        percentage: overallPercentage,
        courses: mergedCourses,
      });

      const recList = recRes?.data?.content || recRes?.content || recRes?.data || (Array.isArray(recRes) ? recRes : []);
      setRecords(recList);
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.message || err.message || 'Failed to load attendance records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (loading) return <LoadingSpinner message="Loading attendance records..." />;
  if (error) return <ErrorState message={error} onRetry={fetchData} />;

  // Course Breakdown Table Columns per Requirement 4:
  // Course, Type, Classes Held, Present, Absent, Attendance %
  const courseColumns = [
    {
      header: 'Course',
      render: (c) => (
        <div>
          <div className="font-semibold text-theme-primary">{c.courseName}</div>
          <div className="flex items-center gap-2 mt-0.5 text-xs flex-wrap">
            <span className="font-mono text-theme-brand font-semibold">{c.courseCode}</span>
            {c.facultyName && (
              <span className="text-theme-secondary text-[11px]">
                • {c.facultyName}
              </span>
            )}
            {c.credits === 0 && (
              <span className="px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 dark:bg-amber-950/40 dark:text-amber-300 text-[10px] font-semibold border border-amber-200 dark:border-amber-800">
                0 Credits (Non-Credit)
              </span>
            )}
          </div>
        </div>
      ),
    },
    {
      header: 'Course Type',
      align: 'center',
      render: (c) => (
        <CourseTypeBadge label={c.courseType === 'LAB' || c.courseType === 'LABORATORY' ? 'LABORATORY' : 'THEORY'} />
      ),
    },
    {
      header: 'Classes Held',
      accessor: 'totalClasses',
      align: 'center',
      render: (c) => <span className="font-semibold text-theme-primary">{c.totalClasses ?? 0}</span>,
    },
    {
      header: 'Present',
      accessor: 'presentCount',
      align: 'center',
      render: (c) => (
        <span className="font-semibold text-emerald-600 dark:text-emerald-400">{c.presentCount ?? 0}</span>
      ),
    },
    {
      header: 'Absent',
      accessor: 'absentCount',
      align: 'center',
      render: (c) => (
        <span className="font-semibold text-rose-600 dark:text-rose-400">{c.absentCount ?? 0}</span>
      ),
    },
    {
      header: 'Attendance %',
      accessor: 'attendancePercentage',
      align: 'right',
      render: (c) => {
        const pct = c.attendancePercentage != null ? c.attendancePercentage : (c.totalClasses > 0 ? (c.presentCount / c.totalClasses) * 100 : 0);
        const isGood = pct >= 75;
        return (
          <span
            className={`inline-flex items-center font-bold px-2.5 py-0.5 rounded text-xs text-white shadow-xs ${
              isGood
                ? 'bg-emerald-600 border border-emerald-700'
                : 'bg-rose-600 border border-rose-700'
            }`}
          >
            {pct.toFixed(1)}%
          </span>
        );
      },
    },
  ];

  const logColumns = [
    {
      header: 'Date',
      accessor: 'attendanceDate',
      cellClassName: 'font-mono text-theme-primary font-semibold',
      render: (r) => formatDateDDMMYYYY(r.attendanceDate),
    },
    { header: 'Course Code', accessor: 'courseCode', cellClassName: 'font-mono text-theme-brand font-semibold' },
    { header: 'Course Name', accessor: 'courseName' },
    {
      header: 'Type',
      render: (r) => <CourseTypeBadge type={r.courseType} />,
    },
    { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status} /> },
    { header: 'Marked By', accessor: 'markedByFacultyName', render: (r) => r.markedByFacultyName || 'Course Coordinator' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Attendance Records"
        subtitle="Track class attendance, course-level metrics, and individual session logs"
      />

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          title="Overall Attendance"
          value={summary?.percentage != null ? `${summary.percentage.toFixed(1)}%` : 'N/A'}
          subtitle={summary?.percentage >= 75 ? 'Meets 75% requirement' : 'Shortage alert'}
          icon={FiCheckSquare}
          color={summary?.percentage >= 75 ? 'emerald' : 'rose'}
        />
        <StatCard
          title="Total Classes Held"
          value={summary?.totalClasses ?? 0}
          subtitle="All registered subjects"
          icon={FiClock}
          color="primary"
        />
        <StatCard
          title="Classes Present"
          value={summary?.presentClasses ?? 0}
          subtitle="Attended sessions"
          icon={FiCheck}
          color="emerald"
        />
        <StatCard
          title="Classes Absent"
          value={summary?.absentClasses ?? 0}
          subtitle="Missed sessions"
          icon={FiXCircle}
          color="rose"
        />
      </div>

      {/* Course Breakdown Table per Requirement 4 */}
      <div className="bg-theme-surface p-5 rounded-2xl border border-theme shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiLayers className="text-theme-brand" />
              <span>Course Breakdown</span>
            </h2>
            <p className="text-xs text-theme-secondary mt-0.5">
              Subject-wise attendance compliance and percentages
            </p>
          </div>
          <span className="text-xs font-mono font-semibold px-2.5 py-1 rounded bg-theme-elevated text-theme-primary border border-theme">
            Semester III
          </span>
        </div>

        <Table
          columns={courseColumns}
          data={summary?.courses || []}
          emptyMessage="No course attendance summary available."
          keyField="courseCode"
        />
      </div>

      {/* Detailed Attendance Session History */}
      <div className="bg-theme-surface p-5 rounded-2xl border border-theme shadow-sm">
        <div className="mb-4">
          <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
            <FiClock className="text-theme-brand" />
            <span>Attendance Log</span>
          </h2>
          <p className="text-xs text-theme-secondary mt-0.5">
            Chronological log of verified class attendance sessions
          </p>
        </div>

        <Table
          columns={logColumns}
          data={records}
          emptyMessage="No attendance session logs recorded yet."
          keyField="attendanceId"
        />
      </div>
    </div>
  );
};

export default StudentAttendancePage;
