import React from 'react';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import Button from './Button';

export const Pagination = ({
  currentPage = 0,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
}) => {
  if (totalPages <= 1) return null;

  const startRecord = currentPage * pageSize + 1;
  const endRecord = Math.min((currentPage + 1) * pageSize, totalElements);

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 py-3 px-2 border-t border-slate-200 text-sm text-slate-600">
      <div>
        Showing <span className="font-semibold text-slate-900">{startRecord}</span> to{' '}
        <span className="font-semibold text-slate-900">{endRecord}</span> of{' '}
        <span className="font-semibold text-slate-900">{totalElements}</span> results
      </div>
      <div className="flex items-center gap-2">
        <Button
          variant="secondary"
          size="sm"
          disabled={currentPage === 0}
          onClick={() => onPageChange(currentPage - 1)}
          icon={FiChevronLeft}
        >
          Previous
        </Button>
        <span className="px-3 py-1 bg-slate-100 rounded-md font-medium text-xs text-slate-700">
          Page {currentPage + 1} of {totalPages}
        </span>
        <Button
          variant="secondary"
          size="sm"
          disabled={currentPage >= totalPages - 1}
          onClick={() => onPageChange(currentPage + 1)}
          icon={FiChevronRight}
        >
          Next
        </Button>
      </div>
    </div>
  );
};

export default Pagination;
