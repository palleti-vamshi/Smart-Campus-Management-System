import React, { useState, useEffect } from 'react';
import { attendanceService } from '../../services/attendanceService';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import { FiCheckSquare, FiXCircle, FiClock, FiCheck } from 'react-icons/fi';

export const StudentAttendancePage = () => {
  const [summary, setSummary] = useState(null);
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [sumRes, recRes] = await Promise.all([
        attendanceService.getMyAttendanceSummary(),
        attendanceService.getMyAttendance(),
      ]);
      setSummary(sumRes.data);
      // recRes might be PageResponse or List
      const recList = recRes.data?.content || recRes.data || [];
      setRecords(recList);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load attendance records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (loading) return <LoadingSpinner message="Loading attendance records..." />;
  if (error) return <ErrorState message={error} onRetry={fetchData} />;

  const columns = [
    { header: 'Date', accessor: 'attendanceDate' },
    { header: 'Course Code', accessor: 'courseCode' },
    { header: 'Course Name', accessor: 'courseName' },
    { header: 'Status', accessor: 'status', render: (r) => <StatusBadge status={r.status} /> },
    { header: 'Remarks', accessor: 'remarks', render: (r) => r.remarks || '-' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Attendance Records"
        subtitle="Track class attendance, overall percentages, and absence logs"
      />

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          title="Overall Attendance"
          value={summary?.percentage != null ? `${summary.percentage.toFixed(1)}%` : 'N/A'}
          icon={FiCheckSquare}
          color={summary?.percentage >= 75 ? 'emerald' : 'rose'}
        />
        <StatCard
          title="Total Classes"
          value={summary?.totalClasses ?? 0}
          icon={FiClock}
          color="primary"
        />
        <StatCard
          title="Present"
          value={summary?.presentClasses ?? 0}
          icon={FiCheck}
          color="emerald"
        />
        <StatCard
          title="Absent"
          value={summary?.absentClasses ?? 0}
          icon={FiXCircle}
          color="rose"
        />
      </div>

      {/* Course Breakdown */}
      {summary?.courseSummaries?.length > 0 && (
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-base font-bold text-slate-800 mb-4">Course Breakdown</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {summary.courseSummaries.map((c) => (
              <div key={c.courseCode} className="p-4 rounded-lg bg-slate-50 border border-slate-200">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-semibold text-slate-800 text-sm">{c.courseName}</h3>
                    <p className="text-xs text-slate-500">{c.courseCode}</p>
                  </div>
                  <span className={`text-sm font-bold ${c.percentage >= 75 ? 'text-emerald-600' : 'text-rose-600'}`}>
                    {c.percentage ? `${c.percentage.toFixed(1)}%` : '0%'}
                  </span>
                </div>
                <div className="mt-3 flex justify-between text-xs text-slate-500 border-t border-slate-200/60 pt-2">
                  <span>Classes: {c.totalClasses}</span>
                  <span>Present: {c.presentClasses}</span>
                  <span>Absent: {c.absentClasses}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Detailed Attendance History */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <h2 className="text-base font-bold text-slate-800 mb-4">Attendance Log</h2>
        <Table
          columns={columns}
          data={records}
          emptyMessage="No attendance records recorded yet."
          keyField="attendanceId"
        />
      </div>
    </div>
  );
};

export default StudentAttendancePage;
