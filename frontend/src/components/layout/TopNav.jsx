import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { FiMenu, FiUser, FiBell } from 'react-icons/fi';
import StatusBadge from '../common/Badge';

export const TopNav = ({ onToggleSidebar }) => {
  const { user, role } = useAuth();

  return (
    <header className="h-16 bg-white border-b border-slate-200 sticky top-0 z-30 flex items-center justify-between px-4 sm:px-6 shadow-xs">
      <div className="flex items-center gap-3">
        <button
          onClick={onToggleSidebar}
          className="lg:hidden p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100 focus:outline-none"
          aria-label="Open sidebar"
        >
          <FiMenu className="w-5 h-5" />
        </button>
        <span className="font-semibold text-slate-800 text-sm hidden sm:inline-block">
          Smart Campus Management System
        </span>
      </div>

      <div className="flex items-center gap-4">
        {/* Role badge */}
        <StatusBadge status={role} />

        {/* User avatar and name */}
        <div className="flex items-center gap-2.5 pl-2 border-l border-slate-200">
          <div className="w-8 h-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-xs">
            {user?.username ? user.username.charAt(0).toUpperCase() : <FiUser />}
          </div>
          <div className="hidden md:block text-left">
            <p className="text-xs font-semibold text-slate-800 leading-tight">
              {user?.username}
            </p>
            <p className="text-[11px] text-slate-500 leading-tight">
              {user?.email || 'Logged in'}
            </p>
          </div>
        </div>
      </div>
    </header>
  );
};

export default TopNav;
