import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import ThemeSelector from '../../components/common/ThemeSelector';
import Button from '../../components/common/Button';
import {
  FiLock,
  FiUser,
  FiEye,
  FiEyeOff,
  FiAlertCircle,
  FiCheckCircle,
  FiLayers,
  FiBookOpen,
  FiCompass,
  FiShield,
} from 'react-icons/fi';

export const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login, isAuthenticated, getDefaultRoute } = useAuth();
  const { currentTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();

  // If already authenticated, redirect
  React.useEffect(() => {
    if (isAuthenticated) {
      const from = location.state?.from?.pathname || getDefaultRoute();
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, location, getDefaultRoute]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username.trim() || !password) {
      setError('Please provide both username and password.');
      return;
    }

    setError('');
    setSubmitting(true);

    try {
      const user = await login({ username: username.trim(), password });
      let target = '/';
      if (user.role === 'ADMIN') target = '/admin/dashboard';
      else if (user.role === 'FACULTY') target = '/faculty/dashboard';
      else if (user.role === 'STUDENT') target = '/student/dashboard';

      const redirectPath = location.state?.from?.pathname || target;
      navigate(redirectPath, { replace: true });
    } catch (err) {
      // Robust contextual error translation
      if (!err.response) {
        if (err.message && err.message.toLowerCase().includes('network')) {
          setError('Unable to connect to the Smart Campus server. Please check your connection.');
        } else {
          setError('Campus services are currently unavailable. Please try again shortly.');
        }
      } else if (err.response.status === 401 || err.response.status === 400) {
        setError('Incorrect username or password. Please verify your credentials.');
      } else if (err.response.status >= 500) {
        setError('Campus services are currently unavailable. The server encountered an error.');
      } else {
        setError(err.response.data?.message || 'Authentication failed. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDemoFill = (u, p) => {
    setUsername(u);
    setPassword(p);
    setError('');
  };

  return (
    <div className="min-h-screen flex flex-col lg:flex-row bg-theme-app text-theme-primary transition-colors">
      {/* LEFT BRAND PANEL */}
      <div
        className="lg:w-1/2 p-8 lg:p-16 flex flex-col justify-between relative overflow-hidden text-white"
        style={{ backgroundColor: 'var(--bg-sidebar)' }}
      >
        {/* Subtle decorative geometric overlay */}
        <div className="absolute inset-0 pointer-events-none opacity-10">
          <svg className="w-full h-full" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="currentColor" strokeWidth="1" />
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#grid)" />
            <circle cx="80%" cy="30%" r="200" fill="none" stroke="currentColor" strokeWidth="2" />
            <circle cx="20%" cy="80%" r="300" fill="none" stroke="currentColor" strokeWidth="2" />
          </svg>
        </div>

        {/* Top bar inside left panel */}
        <div className="relative z-10 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div
              className="w-11 h-11 rounded-xl flex items-center justify-center font-extrabold text-xl shadow-lg border border-white/20"
              style={{ backgroundColor: 'var(--color-primary)' }}
            >
              SC
            </div>
            <div>
              <span className="font-bold tracking-wider text-base uppercase block">SCMS</span>
              <span className="text-xs text-white/70 block -mt-0.5">Smart Campus Management</span>
            </div>
          </div>
          <span className="text-[10px] tracking-widest uppercase px-2.5 py-1 rounded-full bg-white/10 text-white/80 border border-white/10 font-mono">
            v2.4 LTS
          </span>
        </div>

        {/* Central visual statement & campus geometry illustration */}
        <div className="relative z-10 my-10 lg:my-0 space-y-6">
          <div className="space-y-3">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/15 text-xs text-white/90">
              <span className="w-2 h-2 rounded-full animate-ping" style={{ backgroundColor: 'var(--color-accent)' }} />
              AIML • IOT • RAI Department
            </div>
            <h1 className="text-3xl lg:text-4xl xl:text-5xl font-extrabold tracking-tight leading-tight">
              One department.<br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-white via-white/90 to-white/60">
                One connected campus.
              </span>
            </h1>
            <p className="text-white/75 text-sm lg:text-base max-w-md leading-relaxed font-light">
              Academic management reimagined. Unified master data, real-time attendance, digital examinations, and verified credentials.
            </p>
          </div>

          {/* Academic Geometry SVG Illustration */}
          <div className="py-4">
            <svg
              className="w-full max-w-sm h-36 opacity-90"
              viewBox="0 0 360 140"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              {/* Connected node graph representation */}
              <circle cx="50" cy="70" r="28" fill="var(--color-primary)" fillOpacity="0.4" stroke="white" strokeWidth="1.5" />
              <circle cx="180" cy="40" r="32" fill="var(--color-accent)" fillOpacity="0.3" stroke="white" strokeWidth="1.5" />
              <circle cx="310" cy="75" r="26" fill="var(--color-primary)" fillOpacity="0.4" stroke="white" strokeWidth="1.5" />
              <circle cx="180" cy="115" r="20" fill="white" fillOpacity="0.1" stroke="white" strokeWidth="1.2" />

              <line x1="77" y1="62" x2="150" y2="46" stroke="white" strokeOpacity="0.4" strokeDasharray="3 3" strokeWidth="1.5" />
              <line x1="210" y1="46" x2="285" y2="67" stroke="white" strokeOpacity="0.4" strokeDasharray="3 3" strokeWidth="1.5" />
              <line x1="74" y1="81" x2="162" y2="108" stroke="white" strokeOpacity="0.4" strokeDasharray="3 3" strokeWidth="1.5" />
              <line x1="198" y1="108" x2="286" y2="82" stroke="white" strokeOpacity="0.4" strokeDasharray="3 3" strokeWidth="1.5" />
              <line x1="180" y1="72" x2="180" y2="95" stroke="white" strokeOpacity="0.4" strokeWidth="1.5" />

              <text x="50" y="74" fill="white" fontSize="11" fontWeight="600" textAnchor="middle">AIML</text>
              <text x="180" y="44" fill="white" fontSize="12" fontWeight="bold" textAnchor="middle">CORE</text>
              <text x="310" y="79" fill="white" fontSize="11" fontWeight="600" textAnchor="middle">IOT</text>
              <text x="180" y="119" fill="white" fontSize="9" fontWeight="500" textAnchor="middle">RAI</text>
            </svg>
          </div>

          {/* Quick value highlights */}
          <div className="grid grid-cols-3 gap-3 pt-2 border-t border-white/10 text-xs">
            <div className="flex items-center gap-2 text-white/80">
              <FiShield className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>Role Security</span>
            </div>
            <div className="flex items-center gap-2 text-white/80">
              <FiBookOpen className="w-4 h-4 text-sky-400 shrink-0" />
              <span>Curriculum 4.0</span>
            </div>
            <div className="flex items-center gap-2 text-white/80">
              <FiLayers className="w-4 h-4 text-amber-400 shrink-0" />
              <span>Paperless Ops</span>
            </div>
          </div>
        </div>

        {/* Bottom copyright */}
        <div className="relative z-10 pt-4 text-xs text-white/50 flex items-center justify-between">
          <span>Smart Campus Management System</span>
          <span>Faculty & Jury Portal</span>
        </div>
      </div>

      {/* RIGHT LOGIN CARD PANEL */}
      <div className="lg:w-1/2 flex flex-col justify-between p-6 sm:p-12 lg:p-16 bg-theme-app">
        {/* Top row with theme selector */}
        <div className="flex items-center justify-between sm:justify-end gap-3 mb-6">
          <div className="text-xs text-theme-secondary sm:hidden font-medium">SCMS Portal</div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-theme-muted hidden sm:inline">Theme:</span>
            <ThemeSelector variant="dropdown" />
          </div>
        </div>

        {/* Central form container */}
        <div className="w-full max-w-md mx-auto my-auto">
          <div className="bg-theme-surface rounded-2xl border border-theme shadow-xl p-8 sm:p-10 transition-all">
            <div className="mb-6">
              <h2 className="text-2xl font-extrabold tracking-tight text-theme-primary">
                Welcome back
              </h2>
              <p className="text-sm text-theme-secondary mt-1">
                Sign in to access your academic dashboard
              </p>
            </div>

            {error && (
              <div
                className="mb-5 p-3.5 bg-red-50 border border-red-200 dark:bg-red-950/40 dark:border-red-800 rounded-xl flex items-start gap-3 text-xs text-red-700 dark:text-red-300 animate-in fade-in duration-200"
                role="alert"
              >
                <FiAlertCircle className="w-4 h-4 shrink-0 text-red-500 mt-0.5" />
                <span className="font-medium leading-relaxed">{error}</span>
              </div>
            )}

            <form className="space-y-4" onSubmit={handleSubmit}>
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-theme-secondary mb-1.5">
                  Username / Roll Number <span className="text-red-500">*</span>
                </label>
                <div className="relative rounded-lg shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-theme-muted">
                    <FiUser className="h-4 w-4" />
                  </div>
                  <input
                    type="text"
                    required
                    autoComplete="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="e.g. admin, FAC001, or 25071A6601"
                    className="block w-full rounded-lg border border-theme bg-theme-surface text-theme-primary text-sm pl-10 pr-3.5 py-2.5 focus:outline-none focus:ring-2 focus:ring-theme focus:border-theme-primary transition-all"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-theme-secondary mb-1.5">
                  Password <span className="text-red-500">*</span>
                </label>
                <div className="relative rounded-lg shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-theme-muted">
                    <FiLock className="h-4 w-4" />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter your password"
                    className="block w-full rounded-lg border border-theme bg-theme-surface text-theme-primary text-sm pl-10 pr-10 py-2.5 focus:outline-none focus:ring-2 focus:ring-theme focus:border-theme-primary transition-all"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-theme-muted hover:text-theme-primary focus:outline-none"
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? (
                      <FiEyeOff className="h-4 w-4" />
                    ) : (
                      <FiEye className="h-4 w-4" />
                    )}
                  </button>
                </div>
              </div>

              <div className="pt-2">
                <Button
                  type="submit"
                  variant="primary"
                  size="lg"
                  className="w-full text-base font-semibold shadow-md py-3"
                  loading={submitting}
                >
                  {submitting ? 'Signing in...' : 'SIGN IN'}
                </Button>
              </div>
            </form>

            {/* Quick Demo Fill Pills: Only 3 buttons (Student, Faculty - Preety Singh, Administrator) */}
            <div className="mt-6 pt-5 border-t border-theme-subtle">
              <p className="text-[11px] font-semibold uppercase tracking-wider text-theme-muted mb-2 text-center">
                Demo Accounts (Click to Autofill · Password@123)
              </p>
              <div className="grid grid-cols-3 gap-2">
                <button
                  type="button"
                  onClick={() => handleDemoFill('25071A6601', 'Password@123')}
                  className="px-2 py-2 text-xs font-medium rounded-lg border border-theme bg-theme-elevated text-theme-primary hover:border-theme-primary hover:text-theme-brand transition-colors text-center"
                >
                  🎓 Student
                </button>
                <button
                  type="button"
                  onClick={() => handleDemoFill('preety_s', 'Password@123')}
                  className="px-2 py-2 text-xs font-medium rounded-lg border border-theme bg-theme-elevated text-theme-primary hover:border-theme-primary hover:text-theme-brand transition-colors text-center"
                >
                  🧑‍🏫 Faculty
                </button>
                <button
                  type="button"
                  onClick={() => handleDemoFill('admin', 'Password@123')}
                  className="px-2 py-2 text-xs font-medium rounded-lg border border-theme bg-theme-elevated text-theme-primary hover:border-theme-primary hover:text-theme-brand transition-colors text-center"
                >
                  👑 Administrator
                </button>
              </div>
              <p className="text-[10px] text-theme-muted text-center mt-2">
                All 51 faculty accounts from the master registry remain active and can log in with their credentials.
              </p>
            </div>

            {/* Need an account note */}
            <div className="mt-5 text-center">
              <p className="text-xs text-theme-secondary">
                Need an account?{' '}
                <span className="font-semibold text-theme-brand">
                  Contact department administrator
                </span>
              </p>
            </div>
          </div>
        </div>

        {/* Footer info */}
        <div className="text-center pt-6 text-xs text-theme-muted">
          Smart Campus Management System • Secure Enterprise Portal
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
