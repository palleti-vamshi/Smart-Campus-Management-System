import React from 'react';

export const LoadingSpinner = ({ text = 'Loading...', size = 'md' }) => {
  const sizeClasses = {
    sm: 'w-5 h-5 border-2',
    md: 'w-8 h-8 border-3',
    lg: 'w-12 h-12 border-4',
  }[size] || 'w-8 h-8 border-3';

  return (
    <div className="flex flex-col items-center justify-center p-8 space-y-3" role="status" aria-live="polite">
      <div
        className={`${sizeClasses} border-indigo-600 border-t-transparent rounded-full animate-spin`}
      />
      {text && <p className="text-sm font-medium text-slate-500">{text}</p>}
      <span className="sr-only">Loading</span>
    </div>
  );
};

export default LoadingSpinner;
