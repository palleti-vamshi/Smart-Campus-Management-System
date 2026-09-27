import React, { useState, useEffect } from 'react';
import { timetableService } from '../../services/timetableService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const StudentTimetablePage = () => {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState('');

  const fetchTimetable = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await timetableService.getMyTimetable();
      setEntries(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load timetable.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTimetable();
  }, []);

  const filteredEntries = selectedDay
    ? entries.filter((e) => e.dayOfWeek === selectedDay)
    : entries;

  // Sort by day and then start time
  const sorted = [...filteredEntries].sort((a, b) => {
    const dayDiff = DAYS.indexOf(a.dayOfWeek) - DAYS.indexOf(b.dayOfWeek);
    if (dayDiff !== 0) return dayDiff;
    return (a.startTime || '').localeCompare(b.startTime || '');
  });

  const columns = [
    { header: 'Day', accessor: 'dayOfWeek', cellClassName: 'font-semibold text-slate-800' },
    { header: 'Time Slot', render: (r) => `${r.startTime} - ${r.endTime}` },
    { header: 'Course', accessor: 'courseName', render: (r) => `${r.courseName} (${r.courseCode})` },
    { header: 'Faculty', accessor: 'facultyName' },
    { header: 'Classroom', accessor: 'classroomRoomNumber', render: (r) => `${r.classroomRoomNumber || r.roomNumber} - ${r.classroomBuilding || r.building || ''}` },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Weekly Academic Timetable"
        subtitle="Lecture schedule, faculty assignments, and classroom locations"
      >
        <div className="flex items-center gap-2">
          <button
            onClick={() => setSelectedDay('')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
              !selectedDay ? 'bg-primary-700 text-white' : 'bg-white text-slate-600 border border-slate-200'
            }`}
          >
            All Days
          </button>
          {DAYS.map((day) => (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                selectedDay === day ? 'bg-primary-700 text-white' : 'bg-white text-slate-600 border border-slate-200'
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

export default StudentTimetablePage;
