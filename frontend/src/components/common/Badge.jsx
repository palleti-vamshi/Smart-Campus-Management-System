import React from 'react';

export const CourseTypeBadge = ({ type, label, className = '' }) => {
  if (!type && !label) return null;
  const raw = String(label || type).toUpperCase().trim();
  const isLab =
    raw === 'LAB' ||
    raw === 'LABORATORY' ||
    raw === 'PRACTICAL' ||
    raw.includes('LAB') ||
    raw.includes('PRACTICAL');

  const displayLabel = label || (isLab ? (raw === 'PRACTICAL' ? 'PRACTICAL' : 'LABORATORY') : 'THEORY');

  if (isLab) {
    return (
      <span
        className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-0.5 rounded shadow-xs bg-emerald-600 text-white border border-emerald-700 ${className}`}
      >
        {displayLabel}
      </span>
    );
  }
  return (
    <span
      className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-0.5 rounded shadow-xs bg-blue-600 text-white border border-blue-700 ${className}`}
    >
      {displayLabel}
    </span>
  );
};

export const StatusBadge = ({ status, children, className = '' }) => {
  const rawValue = status !== undefined && status !== null ? status : (typeof children === 'string' ? children : '');
  if (!rawValue && !children) return null;

  const normalized = String(rawValue).toUpperCase().trim();

  if (normalized === 'THEORY' || normalized === 'LAB' || normalized === 'LABORATORY' || normalized === 'PRACTICAL') {
    return <CourseTypeBadge type={normalized} className={className} />;
  }

  if (normalized === 'IN_PROGRESS' || normalized === 'IN PROGRESS') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-0.5 rounded shadow-xs bg-amber-600 text-white border border-amber-700 ${className}`}>
        IN PROGRESS
      </span>
    );
  }
  if (normalized === 'UPCOMING') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-0.5 rounded shadow-xs bg-sky-600 text-white border border-sky-700 ${className}`}>
        UPCOMING
      </span>
    );
  }
  if (normalized === 'COMPLETED' || normalized === 'CONDUCTED') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-0.5 rounded shadow-xs bg-emerald-600 text-white border border-emerald-700 ${className}`}>
        COMPLETED
      </span>
    );
  }
  if (normalized === 'NOT_CONDUCTED' || normalized === 'NOT CONDUCTED') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-0.5 rounded shadow-xs bg-slate-700 text-white border border-slate-800 ${className}`}>
        NOT CONDUCTED
      </span>
    );
  }
  if (normalized === 'ENROLLED') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-0.5 rounded shadow-xs bg-emerald-600 text-white border border-emerald-700 ${className}`}>
        ENROLLED
      </span>
    );
  }

  // Exam Types
  if (normalized === 'MID_1' || normalized === 'MID-1' || normalized === 'MID 1') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2 py-0.5 rounded shadow-xs bg-indigo-600 text-white border border-indigo-700 uppercase ${className}`}>
        MID 1
      </span>
    );
  }
  if (normalized === 'MID_2' || normalized === 'MID-2' || normalized === 'MID 2') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2 py-0.5 rounded shadow-xs bg-violet-600 text-white border border-violet-700 uppercase ${className}`}>
        MID 2
      </span>
    );
  }
  if (normalized === 'END_SEMESTER' || normalized === 'SEMESTER' || normalized === 'END_SEM') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2 py-0.5 rounded shadow-xs bg-rose-600 text-white border border-rose-700 uppercase ${className}`}>
        END SEM
      </span>
    );
  }
  if (normalized === 'INTERNAL') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2 py-0.5 rounded shadow-xs bg-blue-600 text-white border border-blue-700 uppercase ${className}`}>
        INTERNAL
      </span>
    );
  }

  // Notice Categories - vibrant solid background with high contrast white text
  if (normalized === 'ACADEMIC') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-blue-700 text-white border border-blue-800 uppercase ${className}`}>
        ACADEMIC
      </span>
    );
  }
  if (normalized === 'EXAM') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-purple-700 text-white border border-purple-800 uppercase ${className}`}>
        EXAM
      </span>
    );
  }
  if (normalized === 'EVENT') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-teal-700 text-white border border-teal-800 uppercase ${className}`}>
        EVENT
      </span>
    );
  }
  if (normalized === 'INTERNSHIP') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-sky-700 text-white border border-sky-800 uppercase ${className}`}>
        INTERNSHIP
      </span>
    );
  }
  if (normalized === 'PLACEMENT') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-indigo-700 text-white border border-indigo-800 uppercase ${className}`}>
        PLACEMENT
      </span>
    );
  }
  if (normalized === 'IMPORTANT') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-rose-700 text-white border border-rose-800 uppercase ${className}`}>
        IMPORTANT
      </span>
    );
  }
  if (normalized === 'GENERAL') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-slate-600 text-white border border-slate-700 uppercase ${className}`}>
        GENERAL
      </span>
    );
  }

  // Priorities - strong alert contrast
  if (normalized === 'URGENT') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-red-600 text-white border border-red-700 uppercase ${className}`}>
        URGENT
      </span>
    );
  }
  if (normalized === 'HIGH') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-amber-600 text-white border border-amber-700 uppercase ${className}`}>
        HIGH
      </span>
    );
  }
  if (normalized === 'NORMAL') {
    return (
      <span className={`inline-flex items-center font-bold tracking-wider text-[11px] px-2.5 py-0.5 rounded shadow-xs bg-slate-700 text-white border border-slate-800 uppercase ${className}`}>
        NORMAL
      </span>
    );
  }

  let variantClass = 'bg-slate-700 text-white border-slate-800';
  if (['PRESENT', 'APPROVED', 'ISSUED', 'ACTIVE', 'PASS'].includes(normalized)) {
    variantClass = 'bg-emerald-600 text-white border-emerald-700';
  } else if (['ABSENT', 'REJECTED', 'FAIL'].includes(normalized)) {
    variantClass = 'bg-rose-700 text-white border-rose-800';
  } else if (['LATE', 'UNDER_REVIEW', 'SUBMITTED'].includes(normalized)) {
    variantClass = 'bg-amber-600 text-white border-amber-700';
  } else if (['EXCUSED'].includes(normalized)) {
    variantClass = 'bg-sky-600 text-white border-sky-700';
  }

  const displayText = normalized ? normalized.replace(/_/g, ' ') : children;

  return (
    <span className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2.5 py-1 rounded shadow-xs border ${variantClass} ${className}`}>
      {displayText}
    </span>
  );
};

export const SectionBadge = ({ section, className = '' }) => {
  if (!section) return null;
  const s = String(section).replace(/^SEC(TION)?\s*/i, '').trim().toUpperCase();
  const colorMap = {
    A: 'bg-indigo-600 text-white border-indigo-700',
    B: 'bg-emerald-600 text-white border-emerald-700',
    C: 'bg-amber-600 text-white border-amber-700',
    D: 'bg-purple-600 text-white border-purple-700',
  };
  const colorClass = colorMap[s] || 'bg-slate-700 text-white border-slate-800';

  return (
    <span
      className={`inline-flex items-center font-bold tracking-wider text-[10px] px-2 py-0.5 rounded shadow-xs border ${colorClass} ${className}`}
    >
      Section {s}
    </span>
  );
};

export const Badge = ({
  children,
  status,
  variant = 'default',
  size = 'md',
  className = '',
  ...props
}) => {
  if (status && !children) {
    return <StatusBadge status={status} className={className} {...props} />;
  }

  if (typeof children === 'string' && variant === 'default') {
    const norm = children.trim().toUpperCase();
    if (['THEORY', 'LAB', 'LABORATORY', 'PRACTICAL', 'ACADEMIC', 'EXAM', 'EVENT', 'INTERNSHIP', 'PLACEMENT', 'IMPORTANT', 'GENERAL', 'URGENT', 'HIGH', 'NORMAL', 'IN_PROGRESS', 'IN PROGRESS', 'UPCOMING', 'COMPLETED', 'CONDUCTED', 'NOT_CONDUCTED', 'NOT CONDUCTED', 'ENROLLED', 'MID_1', 'MID-1', 'MID 1', 'MID_2', 'MID-2', 'MID 2', 'END_SEMESTER', 'SEMESTER', 'END_SEM'].includes(norm)) {
      return <StatusBadge status={children} className={className} {...props} />;
    }
  }

  const variantClasses = {
    default: 'bg-slate-700 text-white border-slate-800',
    primary: 'bg-rose-700 text-white border-rose-800',
    success: 'bg-emerald-600 text-white border-emerald-700',
    warning: 'bg-amber-600 text-white border-amber-700',
    danger: 'bg-rose-700 text-white border-rose-800',
    info: 'bg-sky-600 text-white border-sky-700',
    purple: 'bg-indigo-600 text-white border-indigo-700',
  }[variant] || 'bg-slate-700 text-white border-slate-800';

  const sizeClasses = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-xs px-2.5 py-1',
    lg: 'text-sm px-3 py-1.5',
  }[size] || 'text-xs px-2.5 py-1';

  return (
    <span
      className={`inline-flex items-center font-bold rounded shadow-xs border ${variantClasses} ${sizeClasses} ${className}`}
    >
      {children}
    </span>
  );
};

export default StatusBadge;

