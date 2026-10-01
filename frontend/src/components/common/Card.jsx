import React from 'react';

export const Card = ({
  children,
  className = '',
  title = null,
  subtitle = null,
  action = null,
  headerBorder = true,
  noPadding = false,
}) => {
  return (
    <div
      className={`bg-theme-surface rounded-xl border border-theme shadow-sm transition-all duration-150 hover:shadow-md ${className}`}
    >
      {(title || action) && (
        <div
          className={`flex items-center justify-between px-5 py-4 ${
            headerBorder ? 'border-b border-theme-subtle' : ''
          }`}
        >
          <div>
            {title && (
              <h3 className="text-base font-semibold text-theme-primary leading-tight">
                {title}
              </h3>
            )}
            {subtitle && (
              <p className="text-xs text-theme-secondary mt-0.5">{subtitle}</p>
            )}
          </div>
          {action && <div className="flex items-center gap-2">{action}</div>}
        </div>
      )}
      <div className={noPadding ? '' : 'p-5'}>{children}</div>
    </div>
  );
};

export const StatCard = ({
  title,
  value,
  subtitle,
  icon: Icon,
  color = 'primary',
  trend = null,
}) => {
  const colorMap = {
    primary: 'bg-theme-primary-light text-theme-brand border-theme',
    emerald: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800',
    amber: 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800',
    sky: 'bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950/40 dark:text-sky-300 dark:border-sky-800',
    purple: 'bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800',
    rose: 'bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800',
    slate: 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700',
    indigo: 'bg-indigo-50 text-indigo-700 border-indigo-200 dark:bg-indigo-950/40 dark:text-indigo-300 dark:border-indigo-800',
  }[color] || 'bg-theme-primary-light text-theme-brand border-theme';

  return (
    <div className="bg-theme-surface rounded-xl border border-theme p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold text-theme-secondary uppercase tracking-wider">
            {title}
          </p>
          <p className="text-2xl md:text-3xl font-bold text-theme-primary mt-1 tracking-tight">
            {value !== undefined && value !== null ? value : '-'}
          </p>
          {subtitle && (
            <p className="text-xs text-theme-muted mt-1">{subtitle}</p>
          )}
          {trend && (
            <p className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 mt-1">
              {trend}
            </p>
          )}
        </div>
        {Icon && (
          <div
            className={`w-11 h-11 rounded-lg border flex items-center justify-center ${colorMap}`}
          >
            <Icon className="w-5 h-5 shrink-0" aria-hidden="true" />
          </div>
        )}
      </div>
    </div>
  );
};

export default Card;
