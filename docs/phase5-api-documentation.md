# Phase 5 — Attendance Management API Documentation

This document describes the Attendance Management REST APIs connecting students, enrollments, courses, and faculty.

---

## 1. Overview & Data Model

- **Entity**: `Attendance` (`attendance` table)
- **Core Relationships**:
  - `Student` (`student_id`, FK to `students`, mandatory)
  - `Course` (`course_id`, FK to `courses`, mandatory)
  - `Faculty` (`marked_by`, FK to `faculty`, mandatory)
- **Composite Unique Constraint**: `(student_id, course_id, attendance_date)`
  - A student can have at most one attendance record per course on a given calendar date.
  - Attempting to record duplicate attendance returns `HTTP 409 Conflict`.
- **Enrollment Prerequisite**:
  - Attendance can ONLY be recorded for students who are actively enrolled in the course.
  - Attempting to record attendance for an unenrolled student returns `HTTP 409 Conflict`.
- **Status Values**: `PRESENT`, `ABSENT`, `LATE`

---

## 2. Security & Ownership Rules

- **Base URL**: `/api`
- **Authentication**: JWT Bearer token required on all endpoints (`Authorization: Bearer <token>`).
- **Role Permissions**:
  - `ROLE_ADMIN`: Full attendance CRUD (`/api/admin/attendance/**`).
  - `ROLE_FACULTY`:
    - **Marking**: `POST /api/faculty/attendance` — can ONLY mark attendance for courses assigned to the authenticated faculty member.
    - **Viewing**: `GET /api/faculty/attendance` — results strictly restricted to courses assigned to that faculty member (`course.faculty.faculty_id = authenticatedFaculty.faculty_id`).
    - **Updating**: `PUT /api/faculty/attendance/{id}` — can ONLY update attendance records for their assigned courses.
    - **Deleting**: `DELETE /api/faculty/attendance/{id}` — can ONLY delete attendance records for their assigned courses.
    - Identity is derived securely from JWT (`CustomUserDetails.getUserId()`). Client-supplied faculty IDs are never trusted.
    - Attempting to record, view, update, or delete attendance for another instructor's course returns `HTTP 403 Forbidden` (or empty results on filtered listings).
  - `ROLE_STUDENT`:
    - **Viewing**: `GET /api/student/attendance` — read-only access strictly to the authenticated student's own attendance.
    - **Summary**: `GET /api/student/attendance/summary` — course-level aggregated attendance summary.
    - Identity is derived securely from JWT (`CustomUserDetails.getUserId()`).
    - No client-supplied `studentId` is accepted for determining ownership.
    - Blocked from modifying or deleting attendance (`403 Forbidden`).

---

## 3. Endpoints

### 3.1 Faculty: Record Attendance
- **Endpoint**: `POST /api/faculty/attendance`
- **Authorization**: `ROLE_FACULTY`
- **Request Body**:
```json
{
  "studentId": 1,
  "courseId": 2,
  "attendanceDate": "2026-09-28",
  "status": "PRESENT"
}
```
- **Validation**:
  - `studentId`: `@NotNull`, student must exist and must be enrolled in the course.
  - `courseId`: `@NotNull`, course must exist and must be taught by the authenticated faculty member.
  - `attendanceDate`: `@NotNull`, valid date.
  - `status`: `@NotNull`, `PRESENT`, `ABSENT`, or `LATE`.
  - Duplicate check: `(studentId, courseId, attendanceDate)` must not already exist.
- **Response**: `201 Created` (AttendanceResponse)

### 3.2 Faculty: List Assigned Course Attendance
- **Endpoint**: `GET /api/faculty/attendance`
- **Authorization**: `ROLE_FACULTY`
- **Query Parameters**:
  - `courseId` (Long, optional - filtered against faculty's taught courses)
  - `studentId` (Long, optional)
  - `attendanceDate` (LocalDate, optional)
  - `startDate` (LocalDate, optional)
  - `endDate` (LocalDate, optional)
  - `status` (`PRESENT`, `ABSENT`, `LATE`, optional)
  - `page` (default 0), `size` (default 20), `sort` (default "attendanceId,asc")
- **Response**: `200 OK` (PageResponse<AttendanceResponse>)

### 3.3 Faculty: Update Attendance
- **Endpoint**: `PUT /api/faculty/attendance/{id}`
- **Authorization**: `ROLE_FACULTY`
- **Request Body**:
```json
{
  "attendanceDate": "2026-09-29",
  "status": "LATE"
}
```
- **Business Rule**: Student and course remain immutable. Changing `attendanceDate` checks for collision (`409 Conflict`). Only allowed if course is taught by authenticated faculty (`403 Forbidden` otherwise).
- **Response**: `200 OK` (AttendanceResponse)

### 3.4 Faculty: Delete Attendance
- **Endpoint**: `DELETE /api/faculty/attendance/{id}`
- **Authorization**: `ROLE_FACULTY`
- **Business Rule**: Only allowed if course is taught by authenticated faculty (`403 Forbidden` otherwise).
- **Response**: `200 OK`

### 3.5 Admin: List Attendance
- **Endpoint**: `GET /api/admin/attendance`
- **Authorization**: `ROLE_ADMIN`
- **Query Parameters**: `courseId`, `studentId`, `facultyId`, `attendanceDate`, `startDate`, `endDate`, `status`, pagination.
- **Response**: `200 OK` (PageResponse<AttendanceResponse>)

### 3.6 Admin: Get Attendance by ID
- **Endpoint**: `GET /api/admin/attendance/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` or `404 Not Found`

### 3.7 Admin: Record Attendance
- **Endpoint**: `POST /api/admin/attendance`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**: same as Faculty, with optional `markedByFacultyId` (defaults to assigned course instructor).
- **Validation**: Enforces enrollment requirement and uniqueness rule.
- **Response**: `201 Created`

### 3.8 Admin: Update Attendance
- **Endpoint**: `PUT /api/admin/attendance/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 3.9 Admin: Delete Attendance
- **Endpoint**: `DELETE /api/admin/attendance/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 3.10 Student: View Own Attendance
- **Endpoint**: `GET /api/student/attendance`
- **Authorization**: `ROLE_STUDENT`
- **Query Parameters**: `courseId`, `startDate`, `endDate`, `status`, pagination.
- **Response**: `200 OK` (PageResponse<AttendanceResponse>)

### 3.11 Student: View Course Attendance Summary
- **Endpoint**: `GET /api/student/attendance/summary`
- **Authorization**: `ROLE_STUDENT`
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Attendance summary retrieved successfully",
  "data": [
    {
      "courseId": 1,
      "courseCode": "CS301",
      "courseName": "Operating Systems",
      "totalClasses": 20,
      "presentCount": 18,
      "absentCount": 1,
      "lateCount": 1,
      "attendancePercentage": 90.0
    }
  ]
}
```
- **Calculation Formula**:
  - `attendancePercentage = (presentCount / totalClasses) * 100.0` rounded to 2 decimal places.
  - When `totalClasses == 0`: returns `0.0` safely avoiding division by zero.
  - Executed directly as a database aggregation query via JPQL joining enrolled courses with left-joined attendance records.
