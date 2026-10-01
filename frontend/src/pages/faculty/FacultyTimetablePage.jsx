import React, { useState, useEffect } from 'react';
import { timetableService } from '../../services/timetableService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import { CourseTypeBadge, SectionBadge } from '../../components/common/Badge';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const FacultyTimetablePage = () => {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState('');

  const fetchTimetable = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await timetableService.getMyTimetable();
      // Safe normalization against any shape (PageResponse, array, data wrapper, empty)
      const raw = res?.data ?? res;
      const list = Array.isArray(raw)
        ? raw
        : Array.isArray(raw?.content)
        ? raw.content
        : Array.isArray(raw?.data?.content)
        ? raw.data.content
        : Array.isArray(raw?.data)
        ? raw.data
        : [];
      setEntries(Array.isArray(list) ? list : []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load timetable.');
      setEntries([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTimetable();
  }, []);

  const safeEntries = Array.isArray(entries) ? entries : [];
  const filteredEntries = selectedDay
    ? safeEntries.filter((e) => e?.dayOfWeek === selectedDay)
    : safeEntries;

  const sorted = [...filteredEntries].sort((a, b) => {
    const dayDiff = DAYS.indexOf(a?.dayOfWeek) - DAYS.indexOf(b?.dayOfWeek);
    if (dayDiff !== 0) return dayDiff;
    return (a?.startTime || '').localeCompare(b?.startTime || '');
  });

  const columns = [
    {
      header: 'Day',
      accessor: 'dayOfWeek',
      render: (r) => (
        <span className="font-bold text-xs uppercase px-2 py-0.5 rounded bg-theme-elevated text-theme-primary border border-theme">
          {r.dayOfWeek}
        </span>
      ),
    },
    {
      header: 'Time Slot',
      render: (r) => {
        const start = typeof r.startTime === 'string' ? r.startTime.slice(0, 5) : r.startTime;
        const end = typeof r.endTime === 'string' ? r.endTime.slice(0, 5) : r.endTime;
        return <span className="font-mono text-xs font-semibold text-theme-primary">{start} - {end}</span>;
      },
    },
    {
      header: 'Course',
      render: (r) => (
        <div>
          <span className="font-semibold text-sm text-theme-primary block">{r.courseName}</span>
          <span className="font-mono text-xs text-theme-secondary">{r.courseCode}</span>
        </div>
      ),
    },
    {
      header: 'Course Type',
      align: 'center',
      render: (r) => {
        const isLab =
          (r.courseType && String(r.courseType).toUpperCase().includes('LAB')) ||
          (r.roomType && String(r.roomType).toUpperCase().includes('LAB')) ||
          (r.courseName && r.courseName.toUpperCase().includes('LAB'));
        return <CourseTypeBadge type={isLab ? 'LABORATORY' : 'THEORY'} />;
      },
    },
    {
      header: 'Program',
      render: (r) => (
        <span className="text-xs font-medium text-theme-secondary">
          {r.programCode || 'AIML'} (Sem {r.semester || 3})
        </span>
      ),
    },
    {
      header: 'Section',
      align: 'center',
      render: (r) => <SectionBadge section={r.section} />,
    },
    {
      header: 'Classroom / Hall',
      render: (r) => {
        const room = r.classroomRoomNumber || r.roomNumber || 'TBD';
        const bld = r.classroomBuilding || r.building || '';
        return (
          <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-theme-elevated text-theme-primary border border-theme">
            {room} {bld ? `(${bld})` : ''}
          </span>
        );
      },
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Teaching Schedule & Timetable"
        subtitle="Weekly classroom lecture commitments and assigned academic halls"
      >
        <div className="flex items-center gap-1.5 flex-wrap">
          <button
            onClick={() => setSelectedDay('')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-xs ${
              !selectedDay
                ? 'bg-rose-800 text-white border border-rose-900'
                : 'bg-theme-surface text-theme-secondary border border-theme hover:bg-theme-elevated'
            }`}
          >
            All Days
          </button>
          {DAYS.map((day) => (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-xs ${
                selectedDay === day
                  ? 'bg-rose-800 text-white border border-rose-900'
                  : 'bg-theme-surface text-theme-secondary border border-theme hover:bg-theme-elevated'
              }`}
            >
              {day.slice(0, 3)}
            </button>
          ))}
        </div>
      </PageHeader>

      <Table
        columns={columns}
        data={sorted}
        loading={loading}
        error={error}
        onRetry={fetchTimetable}
        emptyMessage="No classes scheduled for the selected filter."
        keyField="timetableId"
      />
    </div>
  );
};

export default FacultyTimetablePage;
