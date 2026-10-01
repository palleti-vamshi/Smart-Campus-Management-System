import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';
import LoadingSpinner from './components/common/LoadingSpinner';

// Route-level Lazy Loading
// Auth Pages
const LoginPage = lazy(() => import('./pages/auth/LoginPage'));
const ForbiddenPage = lazy(() => import('./pages/auth/ForbiddenPage'));
const NotFoundPage = lazy(() => import('./pages/auth/NotFoundPage'));

// Admin Pages
const AdminDashboardPage = lazy(() => import('./pages/admin/AdminDashboardPage'));
const AdminStudentsPage = lazy(() => import('./pages/admin/AdminStudentsPage'));
const AdminFacultyPage = lazy(() => import('./pages/admin/AdminFacultyPage'));
const AdminProgramsPage = lazy(() => import('./pages/admin/AdminProgramsPage'));
const AdminCoursesPage = lazy(() => import('./pages/admin/AdminCoursesPage'));
const AdminEnrollmentsPage = lazy(() => import('./pages/admin/AdminEnrollmentsPage'));
const AdminAttendancePage = lazy(() => import('./pages/admin/AdminAttendancePage'));
const AdminExamsPage = lazy(() => import('./pages/admin/AdminExamsPage'));
const AdminMarksPage = lazy(() => import('./pages/admin/AdminMarksPage'));
const AdminClassroomsPage = lazy(() => import('./pages/admin/AdminClassroomsPage'));
const AdminTimetablePage = lazy(() => import('./pages/admin/AdminTimetablePage'));
const AdminNoticesPage = lazy(() => import('./pages/admin/AdminNoticesPage'));
const AdminDocumentsPage = lazy(() => import('./pages/admin/AdminDocumentsPage'));

// Faculty Pages
const FacultyDashboardPage = lazy(() => import('./pages/faculty/FacultyDashboardPage'));
const FacultyCoursesPage = lazy(() => import('./pages/faculty/FacultyCoursesPage'));
const FacultyEnrollmentsPage = lazy(() => import('./pages/faculty/FacultyEnrollmentsPage'));
const FacultyAttendancePage = lazy(() => import('./pages/faculty/FacultyAttendancePage'));
const FacultyExamsPage = lazy(() => import('./pages/faculty/FacultyExamsPage'));
const FacultyMarksPage = lazy(() => import('./pages/faculty/FacultyMarksPage'));
const FacultyTimetablePage = lazy(() => import('./pages/faculty/FacultyTimetablePage'));
const FacultyNoticesPage = lazy(() => import('./pages/faculty/FacultyNoticesPage'));

// Student Pages
const StudentDashboardPage = lazy(() => import('./pages/student/StudentDashboardPage'));
const StudentProfilePage = lazy(() => import('./pages/student/StudentProfilePage'));
const StudentCoursesPage = lazy(() => import('./pages/student/StudentCoursesPage'));
const StudentAttendancePage = lazy(() => import('./pages/student/StudentAttendancePage'));
const StudentExamsPage = lazy(() => import('./pages/student/StudentExamsPage'));
const StudentMarksPage = lazy(() => import('./pages/student/StudentMarksPage'));
const StudentTimetablePage = lazy(() => import('./pages/student/StudentTimetablePage'));
const StudentNoticesPage = lazy(() => import('./pages/student/StudentNoticesPage'));
const StudentDocumentsPage = lazy(() => import('./pages/student/StudentDocumentsPage'));

const PageLoader = () => (
  <div className="min-h-[50vh] flex items-center justify-center">
    <LoadingSpinner message="Loading application module..." />
  </div>
);

const RootRedirect = () => {
  const { isAuthenticated, getDefaultRoute } = useAuth();
  if (isAuthenticated) {
    return <Navigate to={getDefaultRoute()} replace />;
  }
  return <Navigate to="/login" replace />;
};

export const App = () => {
  return (
    <Suspense fallback={<PageLoader />}>
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

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/404" replace />} />
      </Routes>
    </Suspense>
  );
};

export default App;
