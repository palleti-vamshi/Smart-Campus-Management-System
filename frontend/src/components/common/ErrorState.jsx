import React from 'react';
import { HiOutlineExclamationCircle, HiOutlineRefresh } from 'react-icons/hi';

export const ErrorState = ({
  title = 'Unable to load data',
  message = 'An error occurred while fetching information from the campus server.',
  onRetry = null,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-8 md:p-12 text-center bg-rose-50/50 rounded-xl border border-rose-200">
      <div className="w-12 h-12 rounded-full bg-rose-100 flex items-center justify-center text-rose-600 mb-3">
        <HiOutlineExclamationCircle className="w-6 h-6" aria-hidden="true" />
      </div>
      <h3 className="text-base font-semibold text-rose-900">{title}</h3>
      <p className="text-sm text-rose-700 max-w-md mt-1 mb-4">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-rose-600 hover:bg-rose-700 rounded-lg transition-colors shadow-sm focus:outline-none focus:ring-2 focus:ring-rose-500 focus:ring-offset-2"
        >
          <HiOutlineRefresh className="w-4 h-4" />
          Retry Request
        </button>
      )}
    </div>
  );
};

export default ErrorState;
