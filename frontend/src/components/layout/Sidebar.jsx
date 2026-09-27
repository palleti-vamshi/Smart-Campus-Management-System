import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  FiGrid,
  FiUsers,
  FiUserCheck,
  FiBook,
  FiLayers,
  FiCheckSquare,
  FiCalendar,
  FiFileText,
  FiAward,
  FiClock,
  FiBell,
  FiFolder,
  FiLogOut,
  FiUser,
  FiBookOpen,
} from 'react-icons/fi';

export const Sidebar = ({ isOpen, onClose }) => {
  const { role, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getNavLinks = () => {
    if (role === 'ADMIN') {
      return [
        { label: 'Dashboard', path: '/admin/dashboard', icon: FiGrid },
        { label: 'Students', path: '/admin/students', icon: FiUsers },
        { label: 'Faculty', path: '/admin/faculty', icon: FiUserCheck },
        { label: 'Programs', path: '/admin/programs', icon: FiLayers },
        { label: 'Courses', path: '/admin/courses', icon: FiBook },
        { label: 'Enrollments', path: '/admin/enrollments', icon: FiBookOpen },
        { label: 'Attendance', path: '/admin/attendance', icon: FiCheckSquare },
        { label: 'Exams', path: '/admin/exams', icon: FiCalendar },
        { label: 'Marks', path: '/admin/marks', icon: FiAward },
        { label: 'Classrooms', path: '/admin/classrooms', icon: FiFolder },
        { label: 'Timetable', path: '/admin/timetable', icon: FiClock },
        { label: 'Notice Centre', path: '/admin/notices', icon: FiBell },
        { label: 'Digital Documents', path: '/admin/documents', icon: FiFileText },
      ];
    } else if (role === 'FACULTY') {
      return [
        { label: 'Dashboard', path: '/faculty/dashboard', icon: FiGrid },
        { label: 'My Courses', path: '/faculty/courses', icon: FiBook },
        { label: 'Enrollments', path: '/faculty/enrollments', icon: FiBookOpen },
        { label: 'Attendance', path: '/faculty/attendance', icon: FiCheckSquare },
        { label: 'Exams', path: '/faculty/exams', icon: FiCalendar },
        { label: 'Marks Entry', path: '/faculty/marks', icon: FiAward },
        { label: 'My Timetable', path: '/faculty/timetable', icon: FiClock },
        { label: 'Notices', path: '/faculty/notices', icon: FiBell },
      ];
    } else if (role === 'STUDENT') {
      return [
        { label: 'Dashboard', path: '/student/dashboard', icon: FiGrid },
        { label: 'My Profile', path: '/student/profile', icon: FiUser },
        { label: 'My Courses', path: '/student/courses', icon: FiBook },
        { label: 'Attendance', path: '/student/attendance', icon: FiCheckSquare },
        { label: 'Exams', path: '/student/exams', icon: FiCalendar },
        { label: 'Results & Marks', path: '/student/marks', icon: FiAward },
        { label: 'Timetable', path: '/student/timetable', icon: FiClock },
        { label: 'Notice Board', path: '/student/notices', icon: FiBell },
        { label: 'Certificates', path: '/student/documents', icon: FiFileText },
      ];
    }
    return [];
  };

  const navLinks = getNavLinks();

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-40 w-64 bg-slate-900 text-slate-300 flex flex-col transition-transform duration-200 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand header */}
        <div className="h-16 flex items-center px-6 border-b border-slate-800 shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-primary-600 flex items-center justify-center text-white font-bold text-lg shadow">
              S
            </div>
            <div>
              <div className="text-base font-bold text-white tracking-wide">SCMS</div>
              <div className="text-xs text-slate-400 font-medium">Smart Campus</div>
            </div>
          </div>
        </div>

        {/* Navigation links */}
        <nav className="flex-1 overflow-y-auto px-4 py-4 space-y-1">
          {navLinks.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => {
                  if (window.innerWidth < 1024) onClose();
                }}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-primary-600 text-white font-semibold shadow-sm'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/80'
                  }`
                }
              >
                <Icon className="w-4 h-4 shrink-0" />
                <span className="truncate">{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* User profile & Logout footer */}
        <div className="p-4 border-t border-slate-800 shrink-0">
          <div className="flex items-center justify-between mb-3 px-1">
            <div className="truncate">
              <p className="text-xs font-semibold text-white truncate">{user?.username}</p>
              <p className="text-[10px] text-primary-400 font-semibold uppercase">{role}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-red-600/80 rounded-lg transition-colors"
          >
            <FiLogOut className="w-3.5 h-3.5" />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
