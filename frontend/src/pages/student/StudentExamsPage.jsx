import React, { useState, useEffect } from 'react';
import { examService } from '../../services/examService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import StatusBadge, { CourseTypeBadge } from '../../components/common/Badge';
import { FiCalendar, FiClock } from 'react-icons/fi';
import { ACADEMIC_CALENDAR_2026_27, formatDateDDMMYYYY, formatMarks } from '../../utils/academicCalendar';

export const StudentExamsPage = () => {
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchExams = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await examService.getMyExams();
      const rawList = res?.data?.content || res?.content || res?.data || (Array.isArray(res) ? res : []);
      const list = Array.isArray(rawList) ? [...rawList] : [];
      // Sort chronologically by start date (Requirement 18)
      list.sort((a, b) => (a.examDate || '').localeCompare(b.examDate || ''));
      setExams(list);
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.message || err.message || 'Failed to load exams.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExams();
  }, []);

  const examMilestones = ACADEMIC_CALENDAR_2026_27.MILESTONES.filter((m) => m.isExam);

  const columns = [
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
      header: 'Course Type',
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
    { header: 'Assessment', accessor: 'examName', cellClassName: 'font-semibold text-theme-primary text-sm' },
    {
      header: 'Date',
      accessor: 'examDate',
      align: 'center',
      render: (r) => (
        <span className="font-mono font-semibold text-theme-primary text-xs">
          {formatDateDDMMYYYY(r.examDate)}
        </span>
      ),
    },
    { header: 'Time Slot', render: (r) => (r.startTime && r.endTime ? `${r.startTime} - ${r.endTime}` : '-') },
    {
      header: 'Max Marks',
      accessor: 'maxMarks',
      align: 'right',
      render: (r) => <span className="font-mono font-bold text-sm text-theme-primary">{formatMarks(r.maxMarks)}</span>,
    },
    {
      header: 'Status',
      align: 'center',
      render: (r) => {
        const name = (r.examName || '').toLowerCase();
        const type = (r.examType || '').toUpperCase();
        const isSessional1 = type === 'MID_1' || name.includes('sessional i') || name.includes('sessional 1') || name.includes('sessional – i');
        const isSessional2 = type === 'MID_2' || name.includes('sessional ii') || name.includes('sessional 2') || name.includes('sessional – ii');

        if (isSessional1) {
          return <StatusBadge status="COMPLETED" />;
        }
        if (isSessional2) {
          return <StatusBadge status="IN PROGRESS" />;
        }
        return <StatusBadge status="UPCOMING" />;
      },
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Examinations"
        subtitle="Scheduled mid-term, end-term, and practical assessments"
      />

      {/* Official Academic Calendar Examination Schedule */}
      <div className="bg-theme-surface p-5 rounded-2xl border border-theme shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
          <div>
            <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
              <FiCalendar className="text-theme-brand" />
              <span>Official Academic Calendar Examination Windows (Semester III)</span>
            </h2>
            <p className="text-xs text-theme-secondary mt-0.5">
              Governed by VNR VJIET B.Tech II Year (R25 Regulation) 2026–2027 Calendar
            </p>
          </div>
          <span className="text-xs font-mono font-semibold px-2.5 py-1 rounded bg-theme-elevated text-theme-primary border border-theme self-start sm:self-auto">
            R25 Regulation
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3.5">
          {examMilestones.map((m, idx) => (
            <div
              key={idx}
              className="p-4 rounded-xl border border-theme bg-theme-elevated space-y-2 flex flex-col justify-between shadow-xs"
            >
              <div>
                <div className="flex items-center justify-between gap-1 mb-2">
                  <CourseTypeBadge label={m.type || 'THEORY'} />
                  <StatusBadge status={m.status || 'UPCOMING'} />
                </div>
                <h4 className="font-bold text-sm text-theme-primary leading-tight">
                  {m.milestone}
                </h4>
                <div className="text-xs text-theme-muted font-medium mt-1">
                  Max: {formatMarks(m.maxMarks)} marks
                </div>
              </div>
              <div className="text-xs text-theme-secondary font-mono border-t border-theme-subtle pt-2 flex items-center justify-between font-bold">
                <span>{m.startDate}</span>
                <span>→</span>
                <span>{m.endDate}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Individual Course Assessments Table */}
      <div className="bg-theme-surface p-5 rounded-2xl border border-theme shadow-sm">
        <div className="mb-4">
          <h2 className="text-base font-bold text-theme-primary flex items-center gap-2">
            <FiClock className="text-theme-brand" />
            <span>Course-Specific Scheduled Assessments</span>
          </h2>
          <p className="text-xs text-theme-secondary mt-0.5">
            Internal assessments and dates assigned by course coordinators
          </p>
        </div>

        <Table
          columns={columns}
          data={exams}
          loading={loading}
          error={error}
          onRetry={fetchExams}
          emptyMessage="No individual assessments scheduled for your enrolled courses."
          keyField="examId"
        />
      </div>
    </div>
  );
};

export default StudentExamsPage;

