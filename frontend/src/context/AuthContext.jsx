import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('scms_token') || null);
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('scms_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token && user) {
      localStorage.setItem('scms_token', token);
      localStorage.setItem('scms_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('scms_token');
      localStorage.removeItem('scms_user');
    }
  }, [token, user]);

  const login = async (credentials) => {
    setLoading(true);
    try {
      const response = await authService.login(credentials);
      // Backend returns ApiResponse<AuthResponse>: { success: true, message: "...", data: { token, userId, username, email, role } }
      const authData = response.data;
      const userObj = {
        userId: authData.userId,
        username: authData.username,
        email: authData.email,
        role: authData.role,
        firstName: authData.firstName,
        lastName: authData.lastName,
        fullName: authData.fullName,
        employeeCode: authData.employeeCode,
        rollNumber: authData.rollNumber,
      };
      setToken(authData.token);
      setUser(userObj);
      localStorage.setItem('scms_token', authData.token);
      localStorage.setItem('scms_user', JSON.stringify(userObj));
      return userObj;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('scms_token');
    localStorage.removeItem('scms_user');
  };

  const role = user?.role || null;
  const isAuthenticated = !!token && !!user;

  const getDefaultRoute = () => {
    switch (role) {
      case 'ADMIN':
        return '/admin/dashboard';
      case 'FACULTY':
        return '/faculty/dashboard';
      case 'STUDENT':
        return '/student/dashboard';
      default:
        return '/login';
    }
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        role,
        isAuthenticated,
        loading,
        login,
        logout,
        getDefaultRoute,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
