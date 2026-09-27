# Phase 4 — Enrollment Management API Documentation

This document describes the Enrollment Management REST APIs connecting students to courses.

---

## 1. Overview & Data Model

- **Junction Entity**: `Enrollment` (`enrollments` table)
- **Relationship**: Many-to-Many between `Student` and `Course`
- **Composite Unique Constraint**: `(student_id, course_id, academic_year, semester)`
  - A student cannot be enrolled in the same course more than once for the same academic year and semester.
  - Duplicate enrollment attempts return `HTTP 409 Conflict`.
- **Status Lifecycle**: `ACTIVE`, `DROPPED`, `COMPLETED`

---

## 2. Security & Ownership Rules

- **Base URL**: `/api`
- **Authentication**: JWT Bearer token required on all endpoints (`Authorization: Bearer <token>`).
- **Role Permissions**:
  - `ROLE_ADMIN`: Full CRUD on `/api/admin/enrollments/**`.
  - `ROLE_FACULTY`: Read-only access on `/api/faculty/enrollments`.
    - **Ownership Rule**: Faculty can ONLY view enrollments for courses where `course.faculty_id` matches their own faculty profile.
    - Identity derived securely from JWT (`CustomUserDetails.getUserId()`).
    - Manipulating `facultyId` or requesting another faculty member's course yields only that faculty's assigned courses or empty results.
    - Blocked from modifying or deleting enrollments (`403 Forbidden`).
  - `ROLE_STUDENT`: Read-only access on `/api/student/enrollments`.
    - **Ownership Rule**: Students can ONLY view their own enrollments.
    - Identity derived securely from JWT (`CustomUserDetails.getUserId()`).
    - Does not accept arbitrary `studentId` query parameters to prevent horizontal privilege escalation.
    - Blocked from admin enrollment APIs (`403 Forbidden`).

---

## 3. Endpoints

### 3.1 Admin: List & Filter Enrollments
- **Endpoint**: `GET /api/admin/enrollments`
- **Authorization**: `ROLE_ADMIN`
- **Query Parameters**:
  - `studentId` (Long, optional)
  - `courseId` (Long, optional)
  - `academicYear` (String, optional, e.g. "2024-2025")
  - `semester` (Integer, optional, 1–8)
  - `status` (`ACTIVE`, `DROPPED`, `COMPLETED`, optional)
  - `page` (Integer, default 0)
  - `size` (Integer, default 20)
  - `sort` (String, default "enrollmentId,asc")
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Enrollments retrieved successfully",
  "data": {
    "content": [
      {
        "enrollmentId": 1,
        "studentId": 1,
        "studentRollNumber": "25071A6601",
        "studentName": "Aarav Sharma",
        "courseId": 1,
        "courseCode": "CS301",
        "courseName": "Operating Systems",
        "academicYear": "2024-2025",
        "semester": 3,
        "enrollmentDate": "2024-08-12",
        "status": "ACTIVE"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

### 3.2 Admin: Get Enrollment by ID
- **Endpoint**: `GET /api/admin/enrollments/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` (EnrollmentResponse) or `404 Not Found`

### 3.3 Admin: Create Enrollment
- **Endpoint**: `POST /api/admin/enrollments`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "studentId": 1,
  "courseId": 2,
  "academicYear": "2024-2025",
  "semester": 3,
  "enrollmentDate": "2024-08-12",
  "status": "ACTIVE"
}
```
- **Validation**:
  - `studentId`: `@NotNull`, referenced student must exist.
  - `courseId`: `@NotNull`, referenced course must exist.
  - `academicYear`: `@NotBlank`, max 20 chars.
  - `semester`: `@NotNull`, `@Min(1)`, `@Max(8)`.
  - `status`: optional enum, defaults to `ACTIVE` if null.
  - `enrollmentDate`: optional LocalDate, defaults to `LocalDate.now()` if null.
- **Business Rule**: Rejects duplicate enrollment for `(student, course, academicYear, semester)` with `409 Conflict`.
- **Response**: `201 Created`

### 3.4 Admin: Update Enrollment
- **Endpoint**: `PUT /api/admin/enrollments/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**: same as Create.
- **Business Rule**: Validates that changes do not collide with another existing enrollment (`409 Conflict`).
- **Response**: `200 OK`

### 3.5 Admin: Delete Enrollment
- **Endpoint**: `DELETE /api/admin/enrollments/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` (`"Enrollment deleted successfully"`) or `404 Not Found`

### 3.6 Faculty: View Assigned Course Enrollments
- **Endpoint**: `GET /api/faculty/enrollments`
- **Authorization**: `ROLE_FACULTY`
- **Query Parameters**:
  - `courseId` (Long, optional - filtered against faculty's taught courses)
  - `academicYear` (String, optional)
  - `semester` (Integer, optional)
  - `status` (EnrollmentStatus, optional)
  - `page` (default 0), `size` (default 20), `sort` (default "enrollmentId,asc")
- **Ownership Enforcement**: Restricted strictly to courses where `course.faculty.facultyId = authenticatedFaculty.facultyId`.
- **Response**: `200 OK` (PageResponse<EnrollmentResponse>)

### 3.7 Student: View Own Enrollments
- **Endpoint**: `GET /api/student/enrollments`
- **Authorization**: `ROLE_STUDENT`
- **Query Parameters**:
  - `academicYear` (String, optional)
  - `semester` (Integer, optional)
  - `status` (EnrollmentStatus, optional)
  - `page` (default 0), `size` (default 20), `sort` (default "enrollmentId,asc")
- **Ownership Enforcement**: Derived strictly from JWT `CustomUserDetails.getUserId()`. Cannot view any other student's enrollments.
- **Response**: `200 OK` (PageResponse<EnrollmentResponse>)
