import React, { useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import ThemeSelector from '../common/ThemeSelector';
import { Badge } from '../common/Badge';
import {
  FiMenu,
  FiUser,
  FiLogOut,
  FiChevronDown,
  FiChevronRight,
  FiHome,
  FiDroplet,
  FiShield,
} from 'react-icons/fi';

export const TopNav = ({ onToggleSidebar }) => {
  const { user, role, logout } = useAuth();
  const { currentTheme } = useTheme();
  const location = useLocation();
  const navigate = useNavigate();
  const [profileOpen, setProfileOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown on click outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setProfileOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    setProfileOpen(false);
    logout();
    navigate('/login');
  };

  // Generate dynamic breadcrumbs
  const getBreadcrumbs = () => {
    const segments = location.pathname.split('/').filter(Boolean);
    if (segments.length === 0) return [{ label: 'Dashboard', path: '/' }];

    const breadcrumbs = [];
    let currentPath = '';

    segments.forEach((seg, idx) => {
      currentPath += `/${seg}`;
      let label = seg.charAt(0).toUpperCase() + seg.slice(1);
      if (seg === 'admin') label = 'Admin';
      else if (seg === 'faculty') label = 'Faculty';
      else if (seg === 'student') label = 'Student';
      else if (seg === 'dashboard') label = 'Overview';
      else if (seg === 'notices') label = 'Notice Centre';
      else if (seg === 'documents') label = 'Digital Documents';
      else if (seg === 'timetable') label = 'Timetable';
      else if (seg === 'attendance') label = 'Attendance';
      else if (seg === 'marks') label = 'Marks & Results';
      else if (seg === 'enrollments') label = 'Enrollments';
      else if (seg === 'classrooms') label = 'Classrooms';
      else if (seg === 'programs') label = 'Programs';
      else if (seg === 'courses') label = 'Courses';
      else if (seg === 'exams') label = 'Examinations';
      else if (seg === 'profile') label = 'My Profile';

      breadcrumbs.push({
        label,
        path: currentPath,
        isLast: idx === segments.length - 1,
      });
    });

    return breadcrumbs;
  };

  const breadcrumbs = getBreadcrumbs();

  const handleViewProfile = () => {
    setProfileOpen(false);
    if (role === 'STUDENT') {
      navigate('/student/profile');
    } else {
      navigate(`/${role.toLowerCase()}/dashboard`);
    }
  };

  return (
    <header className="h-16 bg-theme-surface border-b border-theme sticky top-0 z-30 flex items-center justify-between px-4 sm:px-6 shadow-xs transition-colors">
      {/* Left: Mobile Toggle & Dynamic Breadcrumbs */}
      <div className="flex items-center gap-3 min-w-0">
        <button
          onClick={onToggleSidebar}
          className="lg:hidden p-2 rounded-lg text-theme-secondary hover:text-theme-primary hover:bg-theme-elevated focus:outline-none"
          aria-label="Toggle navigation sidebar"
        >
          <FiMenu className="w-5 h-5" />
        </button>

        {/* Breadcrumb Trail */}
        <nav className="flex items-center gap-1.5 text-xs text-theme-secondary overflow-x-auto py-1 min-w-0">
          <Link
            to={`/${role ? role.toLowerCase() : 'admin'}/dashboard`}
            className="flex items-center gap-1 text-theme-muted hover:text-theme-brand font-medium shrink-0"
          >
            <FiHome className="w-3.5 h-3.5" />
          </Link>
          {breadcrumbs.map((crumb, idx) => (
            <React.Fragment key={crumb.path}>
              <FiChevronRight className="w-3 h-3 text-theme-muted shrink-0" />
              {crumb.isLast ? (
                <span className="font-bold text-theme-primary truncate">
                  {crumb.label}
                </span>
              ) : (
                <Link
                  to={crumb.path}
                  className="font-medium text-theme-secondary hover:text-theme-brand truncate"
                >
                  {crumb.label}
                </Link>
              )}
            </React.Fragment>
          ))}
        </nav>
      </div>

      {/* Right: Theme Selector, Role Badge, Profile Dropdown */}
      <div className="flex items-center gap-3 shrink-0">
        {/* Theme Selector */}
        <div className="hidden sm:flex items-center">
          <ThemeSelector variant="dropdown" />
        </div>

        {/* Role badge */}
        <div className="hidden md:block">
          <span
            className="text-xs font-bold uppercase tracking-wider px-2.5 py-1 rounded-full border border-theme"
            style={{
              backgroundColor: 'var(--color-primary-light)',
              color: 'var(--color-primary)',
            }}
          >
            {role}
          </span>
        </div>

        {/* Profile menu dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setProfileOpen(!profileOpen)}
            className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-theme-elevated border border-transparent hover:border-theme transition-all focus:outline-none focus:ring-2 focus:ring-theme"
            aria-expanded={profileOpen}
            aria-haspopup="true"
            aria-label="User profile menu"
          >
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-xs text-white shadow-xs"
              style={{ backgroundColor: 'var(--color-primary)' }}
            >
              {user?.username ? user.username.charAt(0).toUpperCase() : <FiUser />}
            </div>
            <div className="hidden lg:block text-left pr-1">
              <p className="text-xs font-bold text-theme-primary leading-tight">
                {user?.fullName || user?.username}
              </p>
              <p className="text-[10px] text-theme-muted leading-tight">
                {user?.employeeCode || user?.rollNumber || (user?.email ? user.email.split('@')[0] : role)}
              </p>
            </div>
            <FiChevronDown className="w-3.5 h-3.5 text-theme-muted" />
          </button>

          {/* Dropdown Menu */}
          {profileOpen && (
            <div className="absolute right-0 mt-2 w-56 bg-theme-surface rounded-xl shadow-xl border border-theme py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-150">
              <div className="px-4 py-2 border-b border-theme-subtle">
                <p className="text-xs font-bold text-theme-primary truncate">{user?.fullName || user?.username}</p>
                <p className="text-[11px] text-theme-muted truncate">{user?.email || 'Logged in'}</p>
                {user?.employeeCode && (
                  <p className="text-[10px] font-mono text-theme-muted mt-0.5">Emp ID: {user.employeeCode}</p>
                )}
                {user?.rollNumber && (
                  <p className="text-[10px] font-mono text-theme-muted mt-0.5">Roll No: {user.rollNumber}</p>
                )}
                <div className="mt-1.5">
                  <span className="text-[10px] font-semibold uppercase tracking-wider text-theme-brand">
                    Role: {role}
                  </span>
                </div>
              </div>

              <div className="py-1">
                <button
                  onClick={handleViewProfile}
                  className="w-full px-4 py-2 text-xs font-medium text-theme-primary hover:bg-theme-elevated flex items-center gap-2.5 transition-colors text-left"
                >
                  <FiUser className="w-3.5 h-3.5 text-theme-muted" />
                  <span>View Profile</span>
                </button>

                {/* Mobile theme toggle inside dropdown */}
                <div className="px-4 py-2 sm:hidden border-t border-theme-subtle">
                  <p className="text-[10px] font-bold text-theme-muted uppercase tracking-wider mb-2">
                    Select Theme
                  </p>
                  <ThemeSelector variant="dropdown" />
                </div>
              </div>

              <div className="border-t border-theme-subtle pt-1 mt-1">
                <button
                  onClick={handleLogout}
                  className="w-full px-4 py-2 text-xs font-semibold text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30 flex items-center gap-2.5 transition-colors text-left"
                >
                  <FiLogOut className="w-3.5 h-3.5" />
                  <span>Sign Out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default TopNav;
