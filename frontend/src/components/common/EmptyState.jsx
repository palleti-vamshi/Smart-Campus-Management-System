import React from 'react';
import { HiOutlineInbox } from 'react-icons/hi';

export const EmptyState = ({
  icon: Icon = HiOutlineInbox,
  title = 'No records found',
  description = 'There are no items to display at this moment.',
  action = null,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-8 md:p-12 text-center bg-theme-surface rounded-xl border border-theme shadow-sm">
      <div className="w-14 h-14 rounded-full bg-theme-elevated border border-theme flex items-center justify-center text-theme-muted mb-3">
        <Icon className="w-7 h-7" aria-hidden="true" />
      </div>
      <h3 className="text-base font-semibold text-theme-primary">{title}</h3>
      <p className="text-sm text-theme-secondary max-w-sm mt-1 mb-4">{description}</p>
      {action && <div>{action}</div>}
    </div>
  );
};

export default EmptyState;
