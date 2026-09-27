# Phase 11 — Frontend / UI-UX Implementation

## 1. Frontend Architecture
The Smart Campus Management System (SCMS) frontend is built as a single-page application (SPA) consuming the Spring Boot 4 REST API endpoints developed in Phases 1–10. It follows a modular architecture separating services, authentication state, reusable components, and role-scoped pages:

```
UI Pages (Admin / Faculty / Student)
                │
         Reusable Components
                │
          Context Layer (AuthContext)
                │
     Centralized API Services (Axios)
                │
Spring Boot REST Backend (Phases 1–10)
```

## 2. Technology Stack
- **Framework & Build**: React 18 + Vite
- **Styling**: Tailwind CSS with custom academic palette (`#1e3a8a` primary, `#0f172a` slate)
- **Routing**: React Router v6 (`BrowserRouter`, `Routes`, `Route`, `Navigate`)
- **HTTP Client**: Axios with request/response interceptors and automatic JWT attachment
- **Icons**: React Icons (`react-icons/fi`)
- **Visualizations**: Recharts (`BarChart`, `PieChart`, `ResponsiveContainer`)

## 3. Folder Structure
```
frontend/
├── .env.example
├── .env
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
├── index.html
├── src/
│   ├── index.css
│   ├── main.jsx
│   ├── App.jsx
│   ├── context/
│   │   └── AuthContext.jsx
│   ├── services/
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── dashboardService.js
│   │   ├── studentService.js
│   │   ├── facultyService.js
│   │   ├── programService.js
│   │   ├── departmentService.js
│   │   ├── courseService.js
│   │   ├── enrollmentService.js
│   │   ├── attendanceService.js
│   │   ├── examService.js
│   │   ├── markService.js
│   │   ├── classroomService.js
│   │   ├── timetableService.js
│   │   ├── noticeService.js
│   │   └── documentService.js
│   ├── components/
│   │   ├── common/
│   │   │   ├── Badge.jsx
│   │   │   ├── Button.jsx
│   │   │   ├── Card.jsx
│   │   │   ├── ConfirmDialog.jsx
│   │   │   ├── EmptyState.jsx
│   │   │   ├── ErrorState.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── LoadingSpinner.jsx
│   │   │   ├── Modal.jsx
│   │   │   ├── PageHeader.jsx
│   │   │   ├── Pagination.jsx
│   │   │   ├── ProtectedRoute.jsx
│   │   │   ├── SearchBar.jsx
│   │   │   ├── Select.jsx
│   │   │   └── Table.jsx
│   │   └── layout/
│   │       ├── AppLayout.jsx
│   │       ├── Sidebar.jsx
│   │       └── TopNav.jsx
│   └── pages/
│       ├── auth/
│       │   ├── LoginPage.jsx
│       │   ├── ForbiddenPage.jsx
│       │   └── NotFoundPage.jsx
│       ├── admin/
│       │   ├── AdminDashboardPage.jsx
│       │   ├── AdminStudentsPage.jsx
│       │   ├── AdminFacultyPage.jsx
│       │   ├── AdminProgramsPage.jsx
│       │   ├── AdminCoursesPage.jsx
│       │   ├── AdminEnrollmentsPage.jsx
│       │   ├── AdminAttendancePage.jsx
│       │   ├── AdminExamsPage.jsx
│       │   ├── AdminMarksPage.jsx
│       │   ├── AdminClassroomsPage.jsx
│       │   ├── AdminTimetablePage.jsx
│       │   ├── AdminNoticesPage.jsx
│       │   └── AdminDocumentsPage.jsx
│       ├── faculty/
│       │   ├── FacultyDashboardPage.jsx
│       │   ├── FacultyCoursesPage.jsx
│       │   ├── FacultyEnrollmentsPage.jsx
│       │   ├── FacultyAttendancePage.jsx
│       │   ├── FacultyExamsPage.jsx
│       │   ├── FacultyMarksPage.jsx
│       │   ├── FacultyTimetablePage.jsx
│       │   └── FacultyNoticesPage.jsx
│       └── student/
│           ├── StudentDashboardPage.jsx
│           ├── StudentProfilePage.jsx
│           ├── StudentCoursesPage.jsx
│           ├── StudentAttendancePage.jsx
│           ├── StudentExamsPage.jsx
│           ├── StudentMarksPage.jsx
│           ├── StudentTimetablePage.jsx
│           ├── StudentNoticesPage.jsx
│           └── StudentDocumentsPage.jsx
```

## 4. Authentication Flow
1. **Single Login Portal**: User accesses `/login` and provides credentials (`username` or roll number and `password`).
2. **Backend Authentication**: Sends `POST /api/auth/login`. On success, backend returns `{ token, tokenType: "Bearer", userId, username, email, role }`.
3. **Session Persistence**: JWT token and user profile are saved in `localStorage` under `scms_token` and `scms_user`.
4. **Role Routing**: User is redirected to designated role dashboard:
   - `ADMIN` → `/admin/dashboard`
   - `FACULTY` → `/faculty/dashboard`
   - `STUDENT` → `/student/dashboard`
5. **Unauthorized/Expiry Handling**: Axios interceptor listens for HTTP 401, automatically purges cached tokens, and redirects the browser to `/login`.

## 5. Role-Based Routing
- Protected by `ProtectedRoute.jsx`.
- Verifies `isAuthenticated` (token presence); if false, redirects to `/login`.
- Evaluates `allowedRoles` against active role; if unauthorized, redirects to `/403` (Access Denied).
- Undefined paths redirect to `/404` (Not Found).

## 6. API Integration
- Central Axios client configured in `src/services/api.js` using `VITE_API_BASE_URL`.
- Default base URL: `http://localhost:8080`.
- All requests attach the `Authorization: Bearer <token>` header automatically.
- No mock data: all dashboard figures, master lists, rosters, and schedules query live REST endpoints.

## 7. Admin UI
- **Dashboard**: Real-time KPI cards (students, faculty, programs, courses, classrooms), students by program chart, program attendance chart, academic score stats, circular breakdown, document statuses, upcoming exam schedule.
- **Students**: Cohort management, roll number tracking, program filters, semester filters, create/edit modals, deactivation dialog.
- **Faculty**: Professor directory, designations, specializations, contact details, and department allocations.
- **Programs**: Degree management (AIML, IOT, RAI) with duration and department mappings.
- **Courses**: Course curriculum catalog, credit allocations, and faculty instructor assignments.
- **Enrollments**: Student course registrations, academic year batches, and status tracking.
- **Attendance**: Administrative attendance view, manual recording, status adjustments, and date filtering.
- **Exams**: Scheduling department mid-term, end-term, lab, and quiz examinations with weightage.
- **Marks**: Recording and modifying assessment marks with automated grade rendering.
- **Classrooms**: Managing lecture halls, labs, and capacities.
- **Timetable**: Weekly schedule orchestrator preventing room and faculty scheduling conflicts.
- **Notices**: Broadcast circulars across departments with priority badges and expiry limits.
- **Documents**: Reviewing student certificate requests, updating lifecycle states (`SUBMITTED` → `UNDER_REVIEW` → `APPROVED` → `ISSUED` / `REJECTED`), and downloading generated certificates.

## 8. Faculty UI
- **Dashboard**: Assigned courses, student enrollment totals, attendance averages, today's schedule, upcoming assessments, recent department circulars, and quick actions.
- **My Courses**: Enrolled student count, semester, and credits for assigned subjects.
- **Enrollments**: Roster of enrolled students filtered by course.
- **Attendance**: Daily attendance logging (`PRESENT`, `ABSENT`, `LATE`, `EXCUSED`), modification, and deletion.
- **Exams**: Examination scheduling and syllabus assessments for taught courses.
- **Marks Entry**: Score recording and performance feedback.
- **My Timetable**: Weekly day-by-day teaching lecture schedule.
- **Notices**: Authoring and publishing announcements for students and department colleagues.

## 9. Student UI
- **Dashboard**: Student welcome banner (roll number, program, semester, section), KPIs, course-wise attendance chart, upcoming exams timeline, latest published marks, today's lecture schedule, circulars, and document status summary.
- **Profile**: Full academic record and contact information.
- **My Courses**: Registered subjects with credit breakdowns.
- **Attendance**: Overall percentage, class counts (total, present, absent), and course-wise breakdown.
- **Exams**: Chronological assessment schedule with start/end time slots and maximum marks.
- **Results & Marks**: Evaluated scores and official grade achievements.
- **Timetable**: Weekly class schedule with interactive day filters.
- **Notice Board**: Department announcements with category filters and search.
- **Certificates & Documents**: Bonafide and transcript request form with tracking and PDF certificate download.

## 10. Responsive Design & Accessibility
- Mobile-collapsible navigation drawer with backdrop.
- Flexible CSS grid layouts adapting from mobile (single column) to desktop (multi-column).
- Horizontally scrollable tables with sticky headers.
- Semantic HTML elements, accessible form labels, keyboard navigation, and high-contrast color scheme.

## 11. Component Design
- Reusable UI primitives: `Button`, `Input`, `Select`, `Modal`, `ConfirmDialog`, `Table`, `Pagination`, `SearchBar`, `PageHeader`, `Badge`, `StatusBadge`, `StatCard`, `LoadingSpinner`, `EmptyState`, and `ErrorState`.

## 12. Running the Frontend Locally
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start Vite development server
npm run dev

# Build for production
npm run build
```
Frontend runs at `http://localhost:5173` and proxies API requests to `http://localhost:8080`.
