import React from 'react';

export const Badge = ({
  children,
  variant = 'default',
  size = 'md',
  className = '',
}) => {
  const variantClasses = {
    default: 'bg-slate-100 text-slate-700 border-slate-200',
    primary: 'bg-indigo-50 text-indigo-700 border-indigo-200',
    success: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    warning: 'bg-amber-50 text-amber-700 border-amber-200',
    danger: 'bg-rose-50 text-rose-700 border-rose-200',
    info: 'bg-sky-50 text-sky-700 border-sky-200',
    purple: 'bg-purple-50 text-purple-700 border-purple-200',
  }[variant] || 'bg-slate-100 text-slate-700 border-slate-200';

  const sizeClasses = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-xs px-2.5 py-1',
    lg: 'text-sm px-3 py-1.5',
  }[size] || 'text-xs px-2.5 py-1';

  return (
    <span
      className={`inline-flex items-center font-medium rounded-full border ${variantClasses} ${sizeClasses} ${className}`}
    >
      {children}
    </span>
  );
};

export const StatusBadge = ({ status }) => {
  if (!status) return null;

  const normalized = String(status).toUpperCase();

  let variant = 'default';
  if (['PRESENT', 'APPROVED', 'ISSUED', 'ACTIVE'].includes(normalized)) {
    variant = 'success';
  } else if (['ABSENT', 'REJECTED', 'URGENT'].includes(normalized)) {
    variant = 'danger';
  } else if (['LATE', 'UNDER_REVIEW', 'HIGH', 'SUBMITTED'].includes(normalized)) {
    variant = 'warning';
  } else if (['EXCUSED', 'COMPLETED', 'EVENT', 'EXAM'].includes(normalized)) {
    variant = 'info';
  } else if (['ACADEMIC', 'INTERNSHIP', 'PLACEMENT'].includes(normalized)) {
    variant = 'purple';
  }

  return <Badge variant={variant}>{normalized.replace(/_/g, ' ')}</Badge>;
};

export default Badge;
