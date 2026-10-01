import React from 'react';
import LoadingSpinner from './LoadingSpinner';
import EmptyState from './EmptyState';
import ErrorState from './ErrorState';
import { TableSkeleton } from './Skeleton';

export const Table = ({
  columns = [],
  data = [],
  loading = false,
  error = null,
  onRetry,
  emptyMessage = 'No records found',
  keyField = 'id',
}) => {
  const rows = Array.isArray(data) ? data : (data?.content && Array.isArray(data.content) ? data.content : []);

  if (loading) {
    return <TableSkeleton rows={5} cols={columns.length || 4} />;
  }

  if (error) {
    return (
      <div className="bg-theme-surface rounded-xl border border-theme p-8 shadow-sm">
        <ErrorState message={error} onRetry={onRetry} />
      </div>
    );
  }

  if (rows.length === 0) {
    return (
      <div className="bg-theme-surface rounded-xl border border-theme p-8 shadow-sm">
        <EmptyState title={emptyMessage} />
      </div>
    );
  }

  return (
    <div className="bg-theme-surface rounded-xl border border-theme shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse text-sm">
          <thead>
            <tr className="bg-theme-elevated border-b border-theme text-theme-secondary uppercase text-xs tracking-wider">
              {columns.map((col, index) => (
                <th
                  key={col.key || index}
                  scope="col"
                  className={`px-4 py-3.5 font-bold ${col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'} ${col.className || ''}`}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-theme-subtle">
            {rows.map((row, rowIndex) => {
              const rowKey = row[keyField] !== undefined ? row[keyField] : rowIndex;
              return (
                <tr
                  key={rowKey}
                  className="hover:bg-theme-elevated/80 transition-colors"
                >
                  {columns.map((col, colIndex) => {
                    const value = col.render ? col.render(row, rowIndex) : row[col.accessor || col.key];
                    return (
                      <td
                        key={col.key || colIndex}
                        className={`px-4 py-3 text-theme-primary ${col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'} ${col.cellClassName || ''}`}
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
