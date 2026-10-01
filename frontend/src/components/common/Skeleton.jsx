import React from 'react';

export const Skeleton = ({ className = '', variant = 'rect' }) => {
  const variantClasses = {
    text: 'h-4 w-full rounded',
    circle: 'rounded-full',
    rect: 'rounded-xl',
  }[variant] || 'rounded-lg';

  return (
    <div
      className={`animate-pulse bg-slate-200/80 dark:bg-slate-700/60 ${variantClasses} ${className}`}
      aria-hidden="true"
    />
  );
};

export const StatCardSkeleton = () => (
  <div className="bg-theme-surface rounded-xl border border-theme p-5 shadow-sm">
    <div className="flex items-start justify-between">
      <div className="w-2/3 space-y-2.5">
        <Skeleton variant="text" className="h-3.5 w-24" />
        <Skeleton variant="text" className="h-8 w-20" />
        <Skeleton variant="text" className="h-3 w-32" />
      </div>
      <Skeleton variant="circle" className="w-11 h-11" />
    </div>
  </div>
);

export const ChartSkeleton = ({ height = 'h-72' }) => (
  <div className="bg-theme-surface rounded-xl border border-theme p-5 shadow-sm space-y-4">
    <div className="flex justify-between items-center">
      <Skeleton variant="text" className="h-5 w-40" />
      <Skeleton variant="text" className="h-4 w-20" />
    </div>
    <div className={`w-full ${height} flex items-end gap-3 pt-6`}>
      <Skeleton className="w-1/6 h-1/3" />
      <Skeleton className="w-1/6 h-3/4" />
      <Skeleton className="w-1/6 h-1/2" />
      <Skeleton className="w-1/6 h-5/6" />
      <Skeleton className="w-1/6 h-2/3" />
      <Skeleton className="w-1/6 h-full" />
    </div>
  </div>
);

export const TableSkeleton = ({ rows = 5, cols = 4 }) => (
  <div className="bg-theme-surface rounded-xl border border-theme p-5 shadow-sm space-y-4">
    <div className="flex justify-between items-center pb-2 border-b border-theme-subtle">
      <Skeleton variant="text" className="h-5 w-32" />
      <Skeleton variant="text" className="h-8 w-48 rounded-lg" />
    </div>
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4 items-center py-2">
          {Array.from({ length: cols }).map((_, j) => (
            <Skeleton key={j} variant="text" className="h-4 flex-1" />
          ))}
        </div>
      ))}
    </div>
  </div>
);

export default Skeleton;
