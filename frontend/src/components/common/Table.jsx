import React from 'react';
import LoadingSpinner from './LoadingSpinner';
import EmptyState from './EmptyState';
import ErrorState from './ErrorState';

export const Table = ({
  columns = [],
  data = [],
  loading = false,
  error = null,
  onRetry,
  emptyMessage = 'No records found',
  keyField = 'id',
}) => {
  if (loading) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-8 shadow-sm">
        <LoadingSpinner message="Loading records..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-8 shadow-sm">
        <ErrorState message={error} onRetry={onRetry} />
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-8 shadow-sm">
        <EmptyState title={emptyMessage} />
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase text-xs tracking-wider">
              {columns.map((col, index) => (
                <th
                  key={col.key || index}
                  scope="col"
                  className={`px-4 py-3.5 font-semibold ${col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'} ${col.className || ''}`}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {data.map((row, rowIndex) => {
              const rowKey = row[keyField] !== undefined ? row[keyField] : rowIndex;
              return (
                <tr
                  key={rowKey}
                  className="hover:bg-slate-50/75 transition-colors"
                >
                  {columns.map((col, colIndex) => {
                    const value = col.render ? col.render(row, rowIndex) : row[col.accessor || col.key];
                    return (
                      <td
                        key={col.key || colIndex}
                        className={`px-4 py-3 text-slate-700 ${col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'} ${col.cellClassName || ''}`}
                      >
                        {value}
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Table;
