import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import { FiLock, FiUser, FiAlertCircle } from 'react-icons/fi';

export const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login, isAuthenticated, getDefaultRoute } = useAuth();
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
      // Determine redirection route based on role
      let target = '/';
      if (user.role === 'ADMIN') target = '/admin/dashboard';
      else if (user.role === 'FACULTY') target = '/faculty/dashboard';
      else if (user.role === 'STUDENT') target = '/student/dashboard';

      const redirectPath = location.state?.from?.pathname || target;
      navigate(redirectPath, { replace: true });
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Invalid username or password.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="w-14 h-14 rounded-2xl bg-primary-700 flex items-center justify-center text-white font-extrabold text-2xl shadow-lg">
            SCMS
          </div>
        </div>
        <h2 className="mt-4 text-center text-2xl font-bold tracking-tight text-slate-900">
          Smart Campus Management System
        </h2>
        <p className="mt-1 text-center text-sm text-slate-500">
          AIML · IOT · RAI Department Portal
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-6 shadow-sm border border-slate-200 rounded-2xl sm:px-10">
          <form className="space-y-5" onSubmit={handleSubmit}>
            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2.5 text-xs text-red-700">
                <FiAlertCircle className="w-4 h-4 shrink-0 text-red-500 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <Input
              label="Username or Roll Number"
              name="username"
              type="text"
              autoComplete="username"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. admin, FAC001, or 23AIML001"
              icon={FiUser}
            />

            <Input
              label="Password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              icon={FiLock}
            />

            <div>
              <Button
                type="submit"
                variant="primary"
                size="md"
                className="w-full mt-2"
                loading={submitting}
              >
                Sign In
              </Button>
            </div>
          </form>

          <div className="mt-6 border-t border-slate-100 pt-4 text-center">
            <p className="text-xs text-slate-400">
              Role-based access for Administrator, Faculty, and Student accounts.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
