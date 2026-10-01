export const ACADEMIC_CALENDAR_2026_27 = {
  INSTITUTE: 'VNR Vignana Jyothi Institute of Engineering & Technology',
  PROGRAM: 'B.Tech II Year (R25 Regulation)',
  SEMESTER: 'Semester III (2026–2027)',
  RULES: {
    secondFourthSaturdayHoliday: true,
  },
  MILESTONES: [
    { milestone: 'Commencement of Classes', type: 'INSTRUCTION', startDate: '06-07-2026', endDate: '06-07-2026', duration: 'Commencement', isExam: false },
    { milestone: 'Sessional Examination – I', type: 'THEORY', startDate: '03-08-2026', endDate: '07-08-2026', duration: '03-08-2026 → 07-08-2026', isExam: true, maxMarks: 30, status: 'COMPLETED' },
    { milestone: 'Sessional Examination – II', type: 'THEORY', startDate: '28-09-2026', endDate: '03-10-2026', duration: '28-09-2026 → 03-10-2026', isExam: true, maxMarks: 30, status: 'IN PROGRESS' },
    { milestone: 'Continuous Practical Evaluation', type: 'PRACTICAL', startDate: '26-10-2026', endDate: '31-10-2026', duration: '26-10-2026 → 31-10-2026', isExam: true, maxMarks: 50, status: 'UPCOMING' },
    { milestone: 'End of Classes', type: 'INSTRUCTION', startDate: '31-10-2026', endDate: '31-10-2026', duration: '31-10-2026', isExam: false },
    { milestone: 'Semester End Practical', type: 'PRACTICAL', startDate: '09-11-2026', endDate: '19-11-2026', duration: '09-11-2026 → 19-11-2026', isExam: true, maxMarks: 50, status: 'UPCOMING' },
    { milestone: 'Semester End Examination', type: 'THEORY', startDate: '20-11-2026', endDate: '04-12-2026', duration: '20-11-2026 → 04-12-2026', isExam: true, maxMarks: 60, status: 'UPCOMING' },
  ],
};

export const ACADEMIC_CALENDAR_EVENTS = [
  {
    title: 'Commencement of Classes',
    dateRange: '06-07-2026',
    startDate: '2026-07-06',
    endDate: '2026-07-06',
    category: 'INSTRUCTION',
    description: 'Formal commencement of academic instruction for B.Tech II Year Semester III.',
  },
  {
    title: 'Sessional Examination – I',
    dateRange: '03-08-2026 → 07-08-2026',
    startDate: '2026-08-03',
    endDate: '2026-08-07',
    category: 'EXAM',
    description: 'First Sessional Examination window across all registered theory courses.',
  },
  {
    title: 'Sessional Examination – II',
    dateRange: '28-09-2026 → 03-10-2026',
    startDate: '2026-09-28',
    endDate: '2026-10-03',
    category: 'EXAM',
    description: 'Second Sessional Examination mid-term evaluation window.',
  },
  {
    title: 'Practical',
    dateRange: '26-10-2026 → 31-10-2026',
    startDate: '2026-10-26',
    endDate: '2026-10-31',
    category: 'EXAM',
    description: 'Continuous internal laboratory evaluations and practical assessment.',
  },
  {
    title: 'End of Classes',
    dateRange: '31-10-2026',
    startDate: '2026-10-31',
    endDate: '2026-10-31',
    category: 'INSTRUCTION',
    description: 'Final instructional day of Semester III.',
  },
  {
    title: 'Semester End Practical',
    dateRange: '09-11-2026 → 19-11-2026',
    startDate: '2026-11-09',
    endDate: '2026-11-19',
    category: 'EXAM',
    description: 'Semester-End external practical examinations conducted with external examiners.',
  },
  {
    title: 'Semester End Examination',
    dateRange: '20-11-2026 → 04-12-2026',
    startDate: '2026-11-20',
    endDate: '2026-12-04',
    category: 'EXAM',
    description: 'Official Semester-End theory examinations window.',
  },
];

/**
 * Determines whether a given Date is a holiday according to:
 * 1. Sunday -> Always weekly holiday
 * 2. 2nd Saturday & 4th Saturday -> Holiday
 * 3. Specific official academic calendar non-instructional dates
 */
export function getHolidayInfo(date = new Date()) {
  const day = date.getDay(); // 0 is Sunday, 6 is Saturday
  const dateNum = date.getDate();

  if (day === 0) {
    return { isHoliday: true, reason: 'Sunday — Weekly Holiday' };
  }

  if (day === 6) {
    // Determine which Saturday of the month
    // 1-7: 1st Saturday (Working)
    // 8-14: 2nd Saturday (Holiday)
    // 15-21: 3rd Saturday (Working)
    // 22-28: 4th Saturday (Holiday)
    // 29-31: 5th Saturday (Working)
    if (dateNum >= 8 && dateNum <= 14) {
      return { isHoliday: true, reason: 'Second Saturday — Official Holiday' };
    }
    if (dateNum >= 22 && dateNum <= 28) {
      return { isHoliday: true, reason: 'Fourth Saturday — Official Holiday' };
    }
  }

  return { isHoliday: false, reason: null };
}

/**
 * Checks if current time is within Lunch Break (1:00 PM to 1:40 PM)
 */
export function isLunchBreak(now = new Date()) {
  const hours = now.getHours();
  const minutes = now.getMinutes();
  const timeInMinutes = hours * 60 + minutes;
  // 1:00 PM is 13:00 = 780 min; 1:40 PM is 13:40 = 820 min
  return timeInMinutes >= 780 && timeInMinutes < 820;
}

/**
 * Checks if a timetable slot is currently ongoing
 */
export function isSlotCurrent(startTimeStr, endTimeStr, now = new Date()) {
  if (!startTimeStr || !endTimeStr) return false;
  const parseTimeToMinutes = (t) => {
    const parts = t.split(':').map(Number);
    return parts[0] * 60 + parts[1];
  };
  const nowMinutes = now.getHours() * 60 + now.getMinutes();
  const start = parseTimeToMinutes(startTimeStr);
  const end = parseTimeToMinutes(endTimeStr);
  return nowMinutes >= start && nowMinutes < end;
}

/**
 * Human-readable Program name formatter
 */
export function formatProgramName(code, name) {
  const c = (code || '').toUpperCase();
  if (c.includes('AIML')) return 'CSE – AIML';
  if (c.includes('IOT')) return 'CSE – IoT';
  if (c.includes('RAI')) return 'R&AI';
  return name || code || 'Computer Science';
}

/**
 * Authoritative classroom by program and section
 */
export function getAuthoritativeClassroom(programCode, section = 'A', rollNumber = '') {
  const c = (programCode || '').toUpperCase();
  const s = (section || 'A').toUpperCase().trim();
  const r = (rollNumber || '').toUpperCase().trim();

  const isAiml = c.includes('AIML') || r.includes('66');
  const isIot = c.includes('IOT') || r.includes('69');
  const isRai = c.includes('RAI') || c.includes('ROBOT') || r.includes('39');

  if (isAiml) {
    if (s === 'A') return 'E213';
    if (s === 'B') return 'E214';
    if (s === 'C') return 'E338';
    return 'E213';
  }
  if (isIot) return 'E339';
  if (isRai) return 'E340';

  return 'Room not assigned';
}

/**
 * Format date string to DD-MM-YYYY
 */
export function formatDateDDMMYYYY(dateStr) {
  if (!dateStr) return '-';
  const str = String(dateStr).trim();
  // Already DD-MM-YYYY
  if (/^\d{2}-\d{2}-\d{4}$/.test(str)) return str;
  // YYYY-MM-DD
  if (/^\d{4}-\d{2}-\d{2}/.test(str)) {
    const [y, m, d] = str.slice(0, 10).split('-');
    return `${d}-${m}-${y}`;
  }
  // Try Date parse
  const d = new Date(str);
  if (!isNaN(d.getTime())) {
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}-${month}-${year}`;
  }
  return str;
}

/**
 * Parses date string (DD-MM-YYYY or YYYY-MM-DD) into Date object
 */
export function parseDMY(dateStr) {
  if (!dateStr) return null;
  const str = String(dateStr).trim();
  const parts = str.split('-');
  if (parts.length === 3) {
    if (parts[0].length === 4) {
      // YYYY-MM-DD
      return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
    }
    // DD-MM-YYYY
    return new Date(Number(parts[2]), Number(parts[1]) - 1, Number(parts[0]));
  }
  return new Date(str);
}

/**
 * Dynamically resolves the current or next academic assessment milestone.
 * Default date is 30-09-2026 (current academic cycle date).
 *
 * Rules:
 * 1. Prioritize any assessment currently IN PROGRESS (start <= date <= end).
 * 2. If none is in progress, select the first UPCOMING assessment (date < start).
 * 3. Fallback to the latest completed assessment.
 */
export function getCurrentOrNextAssessment(refDate = '2026-09-30') {
  const d = typeof refDate === 'string' ? parseDMY(refDate) : (refDate || new Date());
  const examMilestones = ACADEMIC_CALENDAR_2026_27.MILESTONES.filter((m) => m.isExam);

  // 1. Check for exam currently IN PROGRESS
  for (const m of examMilestones) {
    const start = parseDMY(m.startDate);
    const end = parseDMY(m.endDate);
    if (start && end) {
      // Ensure end of day inclusion
      end.setHours(23, 59, 59, 999);
      if (d >= start && d <= end) {
        return {
          ...m,
          status: 'IN PROGRESS',
          dateWindow: `${m.startDate} – ${m.endDate}`,
        };
      }
    }
  }

  // 2. Next UPCOMING exam
  for (const m of examMilestones) {
    const start = parseDMY(m.startDate);
    if (start && d < start) {
      return {
        ...m,
        status: 'UPCOMING',
        dateWindow: `${m.startDate} – ${m.endDate}`,
      };
    }
  }

  // 3. Fallback
  const last = examMilestones[examMilestones.length - 1];
  return {
    ...last,
    status: 'COMPLETED',
    dateWindow: `${last.startDate} – ${last.endDate}`,
  };
}

/**
 * Formats marks to whole numbers using Math.round().
 * Handles numbers, numeric strings, and strings with slashes (e.g. "20.63 / 30.00" -> "21 / 30").
 */
export function formatMarks(value) {
  if (value === null || value === undefined || value === '' || value === '-' || value === '—') {
    return '—';
  }
  if (typeof value === 'number') {
    return isNaN(value) ? '—' : String(Math.round(value));
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (trimmed === '' || trimmed === '-' || trimmed === '—') return '—';
    if (trimmed.includes('/')) {
      return trimmed.replace(/(\d+(?:\.\d+)?)/g, (match) => Math.round(Number(match)));
    }
    const num = Number(trimmed);
    if (!isNaN(num)) {
      return String(Math.round(num));
    }
    return trimmed;
  }
  return String(value);
}
