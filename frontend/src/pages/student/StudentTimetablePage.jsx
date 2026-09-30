import React, { useState, useEffect } from 'react';
import { timetableService } from '../../services/timetableService';
import { studentService } from '../../services/studentService';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import Modal from '../../components/common/Modal';
import Button from '../../components/common/Button';
import { CourseTypeBadge } from '../../components/common/Badge';
import {
  ACADEMIC_CALENDAR_EVENTS,
  getHolidayInfo,
  isLunchBreak,
  isSlotCurrent,
  formatProgramName,
  getAuthoritativeClassroom,
} from '../../utils/academicCalendar';
import {
  FiCalendar,
  FiClock,
  FiMapPin,
  FiUser,
  FiBookOpen,
  FiCoffee,
  FiInfo,
  FiCheckCircle,
} from 'react-icons/fi';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const StudentTimetablePage = () => {
  const [entries, setEntries] = useState([]);
  const [studentProfile, setStudentProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState('');
  const [isCalendarModalOpen, setIsCalendarModalOpen] = useState(false);

  const now = new Date();
  const todayDayName = DAYS[now.getDay() - 1] || (now.getDay() === 0 ? 'SUNDAY' : '');
  const holidayInfo = getHolidayInfo(now);
  const isLunch = isLunchBreak(now);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [ttRes, profRes] = await Promise.all([
        timetableService.getMyTimetable(),
        studentService.getMyProfile(),
      ]);
      const timetableList = ttRes.data?.content || ttRes.data || [];
      setEntries(timetableList);
      setStudentProfile(profRes.data || profRes);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load timetable.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (loading) return <LoadingSpinner message="Loading your academic schedule..." />;
  if (error) return <ErrorState message={error} onRetry={fetchData} />;

  const programCode = studentProfile?.programCode || 'AIML';
  const section = studentProfile?.section || 'A';
  const humanProgram = formatProgramName(programCode, studentProfile?.programName);
  const authoritativeRoom = getAuthoritativeClassroom(programCode, section, studentProfile?.rollNumber);

  const filteredEntries = selectedDay
    ? entries.filter((e) => e.dayOfWeek === selectedDay)
    : entries;

  // Group entries by day
  const groupedByDay = DAYS.reduce((acc, day) => {
    acc[day] = filteredEntries
      .filter((e) => e.dayOfWeek === day)
      .sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''));
    return acc;
  }, {});

  const renderSlotCard = (slot, index) => {
    const isLab =
      slot.courseType === 'LAB' ||
      (slot.courseName && slot.courseName.toUpperCase().includes('LAB'));
    const isCurrent =
      slot.dayOfWeek === todayDayName &&
      !holidayInfo.isHoliday &&
      isSlotCurrent(slot.startTime, slot.endTime, now);

    return (
      <div
        key={slot.timetableId || index}
        className={`relative p-4 rounded-xl border transition-all duration-200 ${
          isCurrent
            ? 'bg-amber-500/10 border-amber-500 shadow-md ring-2 ring-amber-500/30'
            : 'bg-theme-surface border-theme hover:border-theme-brand shadow-sm'
        }`}
      >
        {isCurrent && (
          <span className="absolute -top-2.5 right-3 px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-amber-500 text-white shadow-sm flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-white animate-ping" />
            LIVE CLASS
          </span>
        )}

        <div className="flex items-center justify-between gap-2 mb-2">
          <span className="text-xs font-mono font-semibold text-theme-muted flex items-center gap-1">
            <FiClock className="w-3.5 h-3.5 text-theme-brand" />
            {slot.startTime ? slot.startTime.slice(0, 5) : ''} – {slot.endTime ? slot.endTime.slice(0, 5) : ''}
          </span>
          <CourseTypeBadge label={isLab ? 'LABORATORY' : 'THEORY'} />
        </div>

        <h4 className="font-bold text-sm text-theme-primary leading-snug">
          {slot.courseName}
        </h4>
        <p className="text-xs font-mono font-medium text-theme-brand mt-0.5">
          {slot.courseCode}
        </p>

        <div className="mt-3 pt-2.5 border-t border-theme-subtle flex flex-wrap items-center justify-between text-xs text-theme-secondary gap-2">
          <span className="flex items-center gap-1">
            <FiMapPin className="w-3 h-3 text-emerald-600 dark:text-emerald-400 shrink-0" />
            <b className="font-semibold text-theme-primary">
              {slot.classroomRoomNumber || slot.roomNumber || authoritativeRoom}
            </b>
            <span className="text-theme-muted text-[11px]">
              ({slot.classroomBuilding || slot.building || 'CSE Block'})
            </span>
          </span>
          <span className="flex items-center gap-1 truncate max-w-[180px]">
            <FiUser className="w-3 h-3 text-theme-muted shrink-0" />
            <span className="truncate">{slot.facultyName || 'Course Coordinator'}</span>
          </span>
        </div>
      </div>
    );
  };

  const renderLunchBreakSlot = () => (
    <div className="p-3.5 rounded-xl border border-dashed border-amber-400 dark:border-amber-600 bg-amber-500/10 text-theme-primary flex items-center justify-between text-xs shadow-xs">
      <div className="flex items-center gap-2.5">
        <div className="w-8 h-8 rounded-lg bg-amber-500/20 text-amber-700 dark:text-amber-300 flex items-center justify-center shrink-0">
          <FiCoffee className="w-4 h-4" />
        </div>
        <div>
          <span className="font-bold text-theme-primary tracking-wide">LUNCH BREAK</span>
          <p className="text-[11px] text-theme-secondary font-medium">Designated midday instructional recess</p>
        </div>
      </div>
      <span className="font-mono font-bold text-xs text-amber-800 dark:text-amber-200 bg-amber-200/70 dark:bg-amber-900/60 px-2.5 py-1 rounded border border-amber-300 dark:border-amber-700">
        1:00 PM – 1:40 PM
      </span>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header with Title and Actions */}
      <PageHeader
        title="Weekly Academic Timetable"
        subtitle={`Official schedule for ${humanProgram} • Section ${section} • Semester III (2026–2027)`}
      >
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsCalendarModalOpen(true)}
            icon={FiCalendar}
          >
            Academic Calendar
          </Button>
        </div>
      </PageHeader>

      {/* Authoritative Classroom & Academic Meta Bar */}
      <div className="bg-theme-surface rounded-2xl p-4 sm:p-5 border border-theme shadow-sm flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-theme-elevated border border-theme flex items-center justify-center text-theme-brand text-xl font-bold">
            {section}
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-base font-extrabold text-theme-primary">
                {humanProgram} — Section {section}
              </span>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-theme-elevated text-theme-brand border border-theme">
                Room: {authoritativeRoom}
              </span>
            </div>
            <p className="text-xs text-theme-muted mt-0.5">
              Academic Year: <b>2026–2027</b> • Regulation: <b>R25</b> • Class: <b>B.Tech II Year (Semester III)</b>
            </p>
          </div>
        </div>

        {/* Live status badge */}
        <div className="flex items-center gap-2">
          {holidayInfo.isHoliday ? (
            <div className="px-3 py-1.5 rounded-xl bg-rose-600 text-white border border-rose-700 text-xs font-bold flex items-center gap-1.5 shadow-xs">
              <span className="w-2 h-2 rounded-full bg-white" />
              {holidayInfo.reason}
            </div>
          ) : isLunch ? (
            <div className="px-3 py-1.5 rounded-xl bg-amber-600 text-white border border-amber-700 text-xs font-bold flex items-center gap-1.5 shadow-xs">
              <FiCoffee className="w-3.5 h-3.5 text-white" />
              Lunch Break (1:00 – 1:40 PM)
            </div>
          ) : (
            <div className="px-3 py-1.5 rounded-xl bg-emerald-600 text-white border border-emerald-700 text-xs font-bold flex items-center gap-1.5 shadow-xs">
              <span className="w-2 h-2 rounded-full bg-white animate-pulse" />
              Instructional Day ({todayDayName || 'Weekday'})
            </div>
          )}
        </div>
      </div>

      {/* Saturday & Second/Fourth Saturday Holiday Notice */}
      <div className="p-3.5 sm:p-4 rounded-xl bg-theme-elevated border border-theme text-xs text-theme-primary flex items-start gap-3 shadow-xs">
        <div className="p-1.5 rounded-lg bg-theme-brand/10 text-theme-brand shrink-0 mt-0.5">
          <FiInfo className="w-4 h-4 text-theme-brand" />
        </div>
        <div className="leading-relaxed">
          <span className="font-bold text-theme-brand mr-1">Official Holiday Policy:</span>
          <span className="font-medium text-theme-primary">
            Second Saturday and Fourth Saturday of every month are observed as official holidays (no instructional classes). First, Third, and Fifth Saturdays conduct classes according to the Saturday timetable above.
          </span>
        </div>
      </div>

      {/* Day Filter Buttons */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        <button
          onClick={() => setSelectedDay('')}
          className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all shrink-0 ${
            !selectedDay
              ? 'bg-theme-brand text-white shadow-sm'
              : 'bg-theme-surface text-theme-secondary border border-theme hover:bg-theme-elevated'
          }`}
        >
          Full Week View
        </button>
        {DAYS.map((day) => {
          const isToday = day === todayDayName;
          const isSelected = selectedDay === day;
          return (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all shrink-0 flex items-center gap-1.5 ${
                isSelected
                  ? 'bg-theme-brand text-white shadow-sm'
                  : 'bg-theme-surface text-theme-secondary border border-theme hover:bg-theme-elevated'
              }`}
            >
              {day.slice(0, 3)}
              {isToday && (
                <span className={`w-1.5 h-1.5 rounded-full ${isSelected ? 'bg-white' : 'bg-emerald-500'}`} />
              )}
            </button>
          );
        })}
      </div>

      {/* Timetable Display */}
      {selectedDay ? (
        // Single Day Selected
        <div className="bg-theme-surface rounded-2xl p-5 border border-theme shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-theme-subtle">
            <h3 className="font-extrabold text-base text-theme-primary flex items-center gap-2">
              <FiCalendar className="text-theme-brand" />
              <span>{selectedDay} Schedule</span>
            </h3>
            <span className="text-xs text-theme-muted font-medium">
              {(groupedByDay[selectedDay] || []).length} Classes
            </span>
          </div>

          <div className="space-y-3">
            {/* Morning classes (before 1:00 PM) */}
            {groupedByDay[selectedDay]
              ?.filter((s) => (s.startTime || '') < '13:00:00')
              .map((s, idx) => renderSlotCard(s, idx))}

            {/* Authoritative 1:00 - 1:40 PM Lunch Break */}
            {renderLunchBreakSlot()}

            {/* Afternoon classes (from 1:40 PM) */}
            {groupedByDay[selectedDay]
              ?.filter((s) => (s.startTime || '') >= '13:00:00')
              .map((s, idx) => renderSlotCard(s, idx))}

            {(!groupedByDay[selectedDay] || groupedByDay[selectedDay].length === 0) && (
              <p className="text-sm text-theme-muted py-8 text-center">
                No classes scheduled for {selectedDay}.
              </p>
            )}
          </div>
        </div>
      ) : (
        // All Days Grid
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {DAYS.map((day) => {
            const dayClasses = groupedByDay[day] || [];
            const isToday = day === todayDayName;
            return (
              <div
                key={day}
                className={`bg-theme-surface rounded-2xl p-4 border flex flex-col justify-between transition-all ${
                  isToday
                    ? 'border-theme-brand shadow-md ring-1 ring-theme-brand/30'
                    : 'border-theme shadow-sm'
                }`}
              >
                <div>
                  <div className="flex items-center justify-between pb-3 mb-3 border-b border-theme-subtle">
                    <span className="font-extrabold text-sm text-theme-primary flex items-center gap-1.5">
                      {day}
                      {isToday && (
                        <span className="px-2 py-0.2 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
                          TODAY
                        </span>
                      )}
                    </span>
                    <span className="text-[11px] font-semibold text-theme-muted">
                      {dayClasses.length} slots
                    </span>
                  </div>

                  <div className="space-y-2.5">
                    {/* Morning classes */}
                    {dayClasses
                      .filter((s) => (s.startTime || '') < '13:00:00')
                      .map((s, idx) => renderSlotCard(s, idx))}

                    {/* Lunch Break */}
                    {renderLunchBreakSlot()}

                    {/* Afternoon classes */}
                    {dayClasses
                      .filter((s) => (s.startTime || '') >= '13:00:00')
                      .map((s, idx) => renderSlotCard(s, idx))}

                    {dayClasses.length === 0 && (
                      <p className="text-xs text-theme-muted py-6 text-center">
                        No instructional classes scheduled.
                      </p>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Official Academic Calendar Modal */}
      <Modal
        isOpen={isCalendarModalOpen}
        onClose={() => setIsCalendarModalOpen(false)}
        title="Official Academic Calendar 2026–2027 (Semester III)"
      >
        <div className="space-y-4">
          <div className="p-3 rounded-xl bg-theme-elevated border border-theme text-xs text-theme-secondary space-y-1">
            <p><b>Program:</b> {humanProgram} • B.Tech II Year (Semester III)</p>
            <p><b>Regulation:</b> R25 Regulations • VNR VJIET</p>
          </div>

          <div className="divide-y divide-theme-subtle border border-theme rounded-xl overflow-hidden">
            {ACADEMIC_CALENDAR_EVENTS.map((event, idx) => (
              <div key={idx} className="p-3.5 bg-theme-surface hover:bg-theme-elevated transition-colors">
                <div className="flex items-center justify-between gap-2 mb-1">
                  <span className="font-bold text-sm text-theme-primary">{event.title}</span>
                  <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded bg-theme-elevated text-theme-brand border border-theme shrink-0">
                    {event.dateRange}
                  </span>
                </div>
                <p className="text-xs text-theme-secondary">{event.description}</p>
              </div>
            ))}
          </div>

          <div className="flex justify-end pt-2">
            <Button variant="primary" size="sm" onClick={() => setIsCalendarModalOpen(false)}>
              Close Calendar
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default StudentTimetablePage;
