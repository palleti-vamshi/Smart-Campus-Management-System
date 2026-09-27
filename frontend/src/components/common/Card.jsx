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
      className={`bg-white rounded-xl border border-slate-200/90 shadow-sm transition-shadow hover:shadow-md/50 ${className}`}
    >
      {(title || action) && (
        <div
          className={`flex items-center justify-between px-5 py-4 ${
            headerBorder ? 'border-b border-slate-100' : ''
          }`}
        >
          <div>
            {title && (
              <h3 className="text-base font-semibold text-slate-900 leading-tight">
                {title}
              </h3>
            )}
            {subtitle && (
              <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>
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
  color = 'indigo',
  trend = null,
}) => {
  const colorMap = {
    indigo: 'bg-indigo-50 text-indigo-600 border-indigo-100',
    emerald: 'bg-emerald-50 text-emerald-600 border-emerald-100',
    amber: 'bg-amber-50 text-amber-600 border-amber-100',
    sky: 'bg-sky-50 text-sky-600 border-sky-100',
    purple: 'bg-purple-50 text-purple-600 border-purple-100',
    rose: 'bg-rose-50 text-rose-600 border-rose-100',
    slate: 'bg-slate-50 text-slate-600 border-slate-100',
  }[color] || 'bg-indigo-50 text-indigo-600 border-indigo-100';

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">
            {title}
          </p>
          <p className="text-2xl md:text-3xl font-bold text-slate-900 mt-1">
            {value !== undefined && value !== null ? value : '-'}
          </p>
          {subtitle && (
            <p className="text-xs text-slate-500 mt-1">{subtitle}</p>
          )}
          {trend && (
            <p className="text-xs font-medium text-emerald-600 mt-1">
              {trend}
            </p>
          )}
        </div>
        {Icon && (
          <div
            className={`w-11 h-11 rounded-lg border flex items-center justify-center ${colorMap}`}
          >
            <Icon className="w-5 h-5" aria-hidden="true" />
          </div>
        )}
      </div>
    </div>
  );
};

export default Card;
