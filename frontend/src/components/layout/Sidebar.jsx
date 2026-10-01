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

  // Grouped structure per spec
  const getNavSections = () => {
    if (role === 'ADMIN') {
      return [
        {
          heading: 'OVERVIEW',
          links: [{ label: 'Dashboard', path: '/admin/dashboard', icon: FiGrid }],
        },
        {
          heading: 'ACADEMICS',
          links: [
            { label: 'Students', path: '/admin/students', icon: FiUsers },
            { label: 'Faculty', path: '/admin/faculty', icon: FiUserCheck },
            { label: 'Programs', path: '/admin/programs', icon: FiLayers },
            { label: 'Courses', path: '/admin/courses', icon: FiBook },
            { label: 'Enrollments', path: '/admin/enrollments', icon: FiBookOpen },
          ],
        },
        {
          heading: 'ACADEMIC OPERATIONS',
          links: [
            { label: 'Attendance', path: '/admin/attendance', icon: FiCheckSquare },
            { label: 'Exams', path: '/admin/exams', icon: FiCalendar },
            { label: 'Marks', path: '/admin/marks', icon: FiAward },
            { label: 'Classrooms', path: '/admin/classrooms', icon: FiFolder },
            { label: 'Timetable', path: '/admin/timetable', icon: FiClock },
          ],
        },
        {
          heading: 'SERVICES',
          links: [
            { label: 'Notice Centre', path: '/admin/notices', icon: FiBell },
            { label: 'Digital Documents', path: '/admin/documents', icon: FiFileText },
          ],
        },
      ];
    } else if (role === 'FACULTY') {
      return [
        {
          heading: 'OVERVIEW',
          links: [{ label: 'Dashboard', path: '/faculty/dashboard', icon: FiGrid }],
        },
        {
          heading: 'TEACHING',
          links: [
            { label: 'Courses', path: '/faculty/courses', icon: FiBook },
            { label: 'Enrollments', path: '/faculty/enrollments', icon: FiBookOpen },
            { label: 'Attendance', path: '/faculty/attendance', icon: FiCheckSquare },
            { label: 'Exams', path: '/faculty/exams', icon: FiCalendar },
            { label: 'Marks Entry', path: '/faculty/marks', icon: FiAward },
            { label: 'Timetable', path: '/faculty/timetable', icon: FiClock },
          ],
        },
        {
          heading: 'COMMUNICATION',
          links: [{ label: 'Notice Centre', path: '/faculty/notices', icon: FiBell }],
        },
      ];
    } else if (role === 'STUDENT') {
      return [
        {
          heading: 'OVERVIEW',
          links: [{ label: 'Dashboard', path: '/student/dashboard', icon: FiGrid }],
        },
        {
          heading: 'MY ACADEMICS',
          links: [
            { label: 'Profile', path: '/student/profile', icon: FiUser },
            { label: 'Courses', path: '/student/courses', icon: FiBook },
            { label: 'Attendance', path: '/student/attendance', icon: FiCheckSquare },
            { label: 'Exams', path: '/student/exams', icon: FiCalendar },
            { label: 'Marks & Results', path: '/student/marks', icon: FiAward },
            { label: 'Timetable', path: '/student/timetable', icon: FiClock },
          ],
        },
        {
          heading: 'SERVICES',
          links: [
            { label: 'Notices', path: '/student/notices', icon: FiBell },
            { label: 'Documents', path: '/student/documents', icon: FiFileText },
          ],
        },
      ];
    }
    return [];
  };

  const sections = getNavSections();

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/60 backdrop-blur-xs lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* Sidebar container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-40 w-64 text-white flex flex-col transition-transform duration-200 ease-in-out lg:translate-x-0 shadow-2xl border-r border-white/10 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        style={{ backgroundColor: 'var(--bg-sidebar)' }}
      >
        {/* Brand header */}
        <div className="h-16 flex items-center justify-between px-5 border-b border-white/10 shrink-0">
          <div className="flex items-center gap-3">
            <div
              className="w-9 h-9 rounded-xl flex items-center justify-center font-bold text-sm shadow-md border border-white/20"
              style={{ backgroundColor: 'var(--color-primary)' }}
            >
              SC
            </div>
            <div>
              <div className="text-sm font-extrabold tracking-wider text-white">SCMS</div>
              <div className="text-[11px] text-white/70 font-medium">Smart Campus</div>
            </div>
          </div>
          <span
            className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border border-white/20"
            style={{ backgroundColor: 'var(--color-primary-light)', color: 'var(--color-primary)' }}
          >
            {role}
          </span>
        </div>

        {/* Grouped navigation sections */}
        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-5">
          {sections.map((sec) => (
            <div key={sec.heading} className="space-y-1">
              <p className="px-3 text-[10px] font-bold uppercase tracking-wider text-white/40">
                {sec.heading}
              </p>
              <div className="space-y-0.5">
                {sec.links.map((item) => {
                  const Icon = item.icon;
                  return (
                    <NavLink
                      key={item.path}
                      to={item.path}
                      onClick={() => {
                        if (window.innerWidth < 1024) onClose();
                      }}
                      className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-all ${
                          isActive
                            ? 'text-white shadow-md border border-white/15'
                            : 'text-white/70 hover:text-white hover:bg-white/10'
                        }`
                      }
                      style={({ isActive }) => ({
                        backgroundColor: isActive ? 'var(--bg-sidebar-active)' : 'transparent',
                      })}
                    >
                      <Icon className="w-4 h-4 shrink-0" aria-hidden="true" />
                      <span className="truncate">{item.label}</span>
                    </NavLink>
                  );
                })}
              </div>
            </div>
          ))}
        </nav>

        {/* User profile & Logout footer */}
        <div className="p-4 border-t border-white/10 shrink-0 bg-black/15">
          <div className="flex items-center gap-2.5 mb-3 px-1">
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 text-white border border-white/20 shadow-xs"
              style={{ backgroundColor: 'var(--color-primary)' }}
            >
              {user?.username ? user.username.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="truncate flex-1">
              <p className="text-xs font-bold text-white truncate">{user?.fullName || user?.username}</p>
              <p className="text-[10px] text-white/60 truncate">
                {user?.employeeCode ? `Emp: ${user.employeeCode}` : user?.rollNumber ? `Roll: ${user.rollNumber}` : (user?.email || `${role} Account`)}
              </p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 text-xs font-semibold text-white/80 hover:text-white bg-white/5 hover:bg-red-600/80 rounded-lg transition-colors border border-white/10"
            aria-label="Sign out of Smart Campus"
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
