import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';

// Auth Pages
import LoginPage from './pages/auth/LoginPage';
import ForbiddenPage from './pages/auth/ForbiddenPage';
import NotFoundPage from './pages/auth/NotFoundPage';

// Admin Pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminStudentsPage from './pages/admin/AdminStudentsPage';
import AdminFacultyPage from './pages/admin/AdminFacultyPage';
import AdminProgramsPage from './pages/admin/AdminProgramsPage';
import AdminCoursesPage from './pages/admin/AdminCoursesPage';
import AdminEnrollmentsPage from './pages/admin/AdminEnrollmentsPage';
import AdminAttendancePage from './pages/admin/AdminAttendancePage';
import AdminExamsPage from './pages/admin/AdminExamsPage';
import AdminMarksPage from './pages/admin/AdminMarksPage';
import AdminClassroomsPage from './pages/admin/AdminClassroomsPage';
import AdminTimetablePage from './pages/admin/AdminTimetablePage';
import AdminNoticesPage from './pages/admin/AdminNoticesPage';
import AdminDocumentsPage from './pages/admin/AdminDocumentsPage';

// Faculty Pages
import FacultyDashboardPage from './pages/faculty/FacultyDashboardPage';
import FacultyCoursesPage from './pages/faculty/FacultyCoursesPage';
import FacultyEnrollmentsPage from './pages/faculty/FacultyEnrollmentsPage';
import FacultyAttendancePage from './pages/faculty/FacultyAttendancePage';
import FacultyExamsPage from './pages/faculty/FacultyExamsPage';
import FacultyMarksPage from './pages/faculty/FacultyMarksPage';
import FacultyTimetablePage from './pages/faculty/FacultyTimetablePage';
import FacultyNoticesPage from './pages/faculty/FacultyNoticesPage';

// Student Pages
import StudentDashboardPage from './pages/student/StudentDashboardPage';
import StudentProfilePage from './pages/student/StudentProfilePage';
import StudentCoursesPage from './pages/student/StudentCoursesPage';
import StudentAttendancePage from './pages/student/StudentAttendancePage';
import StudentExamsPage from './pages/student/StudentExamsPage';
import StudentMarksPage from './pages/student/StudentMarksPage';
import StudentTimetablePage from './pages/student/StudentTimetablePage';
import StudentNoticesPage from './pages/student/StudentNoticesPage';
import StudentDocumentsPage from './pages/student/StudentDocumentsPage';

const RootRedirect = () => {
  const { isAuthenticated, getDefaultRoute } = useAuth();
  if (isAuthenticated) {
    return <Navigate to={getDefaultRoute()} replace />;
  }
  return <Navigate to="/login" replace />;
};

export const App = () => {
  return (
    <Routes>
      {/* Root redirect */}
      <Route path="/" element={<RootRedirect />} />

      {/* Public / Auth routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/403" element={<ForbiddenPage />} />
      <Route path="/404" element={<NotFoundPage />} />

      {/* ADMIN Routes */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboardPage />} />
        <Route path="students" element={<AdminStudentsPage />} />
        <Route path="faculty" element={<AdminFacultyPage />} />
        <Route path="programs" element={<AdminProgramsPage />} />
        <Route path="courses" element={<AdminCoursesPage />} />
        <Route path="enrollments" element={<AdminEnrollmentsPage />} />
        <Route path="attendance" element={<AdminAttendancePage />} />
        <Route path="exams" element={<AdminExamsPage />} />
        <Route path="marks" element={<AdminMarksPage />} />
        <Route path="classrooms" element={<AdminClassroomsPage />} />
        <Route path="timetable" element={<AdminTimetablePage />} />
        <Route path="notices" element={<AdminNoticesPage />} />
        <Route path="documents" element={<AdminDocumentsPage />} />
      </Route>

      {/* FACULTY Routes */}
      <Route
        path="/faculty"
        element={
          <ProtectedRoute allowedRoles={['FACULTY']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/faculty/dashboard" replace />} />
        <Route path="dashboard" element={<FacultyDashboardPage />} />
        <Route path="courses" element={<FacultyCoursesPage />} />
        <Route path="enrollments" element={<FacultyEnrollmentsPage />} />
        <Route path="attendance" element={<FacultyAttendancePage />} />
        <Route path="exams" element={<FacultyExamsPage />} />
        <Route path="marks" element={<FacultyMarksPage />} />
        <Route path="timetable" element={<FacultyTimetablePage />} />
        <Route path="notices" element={<FacultyNoticesPage />} />
      </Route>

      {/* STUDENT Routes */}
      <Route
        path="/student"
        element={
          <ProtectedRoute allowedRoles={['STUDENT']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/student/dashboard" replace />} />
        <Route path="dashboard" element={<StudentDashboardPage />} />
        <Route path="profile" element={<StudentProfilePage />} />
        <Route path="courses" element={<StudentCoursesPage />} />
        <Route path="attendance" element={<StudentAttendancePage />} />
        <Route path="exams" element={<StudentExamsPage />} />
        <Route path="marks" element={<StudentMarksPage />} />
        <Route path="timetable" element={<StudentTimetablePage />} />
        <Route path="notices" element={<StudentNoticesPage />} />
        <Route path="documents" element={<StudentDocumentsPage />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default App;
