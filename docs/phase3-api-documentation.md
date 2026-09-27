# Phase 3 — Academic Master Data API Documentation

This document specifies the REST APIs implemented in Phase 3 for the Smart Campus Management System.

---

## 1. Overview & Security Architecture

- **Base URL**: `/api`
- **Authentication**: Bearer JWT token required for all endpoints via `Authorization: Bearer <token>`.
- **Role-Based Access Control**:
  - `ROLE_ADMIN`: Full CRUD on Departments, Programs, Students, Faculty, and Courses.
  - `ROLE_STUDENT`: Access to own student profile only (`GET /api/student/profile`). Blocked from admin master-data endpoints (`403 Forbidden`).
  - `ROLE_FACULTY`: Access to own faculty profile (`GET /api/faculty/profile`). Blocked from admin master-data CRUD (`403 Forbidden`).

---

## 2. Department APIs

### 2.1 List Departments
- **Endpoint**: `GET /api/admin/departments`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`
```json
[
  {
    "departmentId": 1,
    "departmentCode": "CSE",
    "departmentName": "Computer Science and Engineering",
    "description": "Department of Computer Science and Engineering",
    "createdAt": "2026-09-27T10:00:00",
    "updatedAt": "2026-09-27T10:00:00"
  }
]
```

### 2.2 Get Department by ID
- **Endpoint**: `GET /api/admin/departments/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` (DepartmentResponse) or `404 Not Found`

### 2.3 Create Department
- **Endpoint**: `POST /api/admin/departments`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "departmentCode": "ECE",
  "departmentName": "Electronics and Communication Engineering",
  "description": "Department of ECE"
}
```
- **Validation**:
  - `departmentCode`: `@NotBlank`, max 20 characters, unique across departments.
  - `departmentName`: `@NotBlank`, 3 to 100 characters.
  - `description`: optional, max 255 characters.
- **Response**: `201 Created`

### 2.4 Update Department
- **Endpoint**: `PUT /api/admin/departments/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` or `404 Not Found` / `409 Conflict` (if code belongs to another department).

### 2.5 Delete Department
- **Endpoint**: `DELETE /api/admin/departments/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Business Rule**: Deletion is rejected (`409 Conflict`) if any programs or faculty are linked to this department.
- **Response**: `204 No Content`

---

## 3. Program APIs

### 3.1 List Programs
- **Endpoint**: `GET /api/admin/programs`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` (List of ProgramResponse)

### 3.2 Get Program by ID
- **Endpoint**: `GET /api/admin/programs/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` or `404 Not Found`

### 3.3 List Programs by Department
- **Endpoint**: `GET /api/admin/departments/{departmentId}/programs`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 3.4 Create Program
- **Endpoint**: `POST /api/admin/programs`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "departmentId": 1,
  "programCode": "AIML",
  "programName": "B.Tech in Artificial Intelligence & Machine Learning",
  "durationYears": 4
}
```
- **Validation**:
  - `departmentId`: `@NotNull`, must reference an existing department.
  - `programCode`: `@NotBlank`, max 20 characters, unique across programs.
  - `programName`: `@NotBlank`, 3 to 100 characters.
  - `durationYears`: `@NotNull`, `@Min(1)`, `@Max(10)`.
- **Response**: `201 Created`

### 3.5 Update Program
- **Endpoint**: `PUT /api/admin/programs/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 3.6 Delete Program
- **Endpoint**: `DELETE /api/admin/programs/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Business Rule**: Deletion is rejected (`409 Conflict`) if any students or courses are linked to this program.
- **Response**: `204 No Content`

---

## 4. Student APIs

### 4.1 List & Filter Students
- **Endpoint**: `GET /api/admin/students`
- **Authorization**: `ROLE_ADMIN`
- **Query Parameters**:
  - `programId` (Integer, optional)
  - `semester` (Integer, optional)
  - `section` (String, optional)
  - `search` (String, optional: matches rollNumber, firstName, lastName, or email)
  - `page` (Integer, default 0)
  - `size` (Integer, default 20)
  - `sort` (String, default "studentId,asc")
- **Response**: `200 OK` (PageResponse with `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`)

### 4.2 Get Student by ID
- **Endpoint**: `GET /api/admin/students/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK` or `404 Not Found` (Note: Password hash is never exposed)

### 4.3 Create Student
- **Endpoint**: `POST /api/admin/students`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "programId": 1,
  "rollNumber": "23071A6601",
  "firstName": "Aarav",
  "lastName": "Sharma",
  "gender": "MALE",
  "dateOfBirth": "2005-06-15",
  "admissionYear": 2023,
  "currentSemester": 3,
  "section": "A",
  "phone": "9876543210"
}
```
- **Business Rules**:
  - Automatically provisions a `User` identity within the same transaction.
  - Username = `rollNumber`
  - Email = `<rollNumber>@vnrvjiet.in`
  - Default password = `Password@123` (BCrypt hashed)
  - Role = `STUDENT`
  - Roll number and email uniqueness enforced.
- **Response**: `201 Created`

### 4.4 Update Student
- **Endpoint**: `PUT /api/admin/students/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Business Rules**:
  - Updates profile fields.
  - If `rollNumber` changes, checks uniqueness, and updates the linked `User` username and email synchronously.
- **Response**: `200 OK`

### 4.5 Delete Student
- **Endpoint**: `DELETE /api/admin/students/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Business Rule**: Rejects deletion (`409 Conflict`) if student has enrollments, attendance records, marks, or document requests. When clean, removes both student profile and user identity.
- **Response**: `204 No Content`

### 4.6 Student Self-Profile
- **Endpoint**: `GET /api/student/profile`
- **Authorization**: `ROLE_STUDENT`
- **Security**: Extracted directly from JWT / SecurityContext (`CustomUserDetails.getUserId()`). No student ID accepted in query/path to prevent horizontal privilege escalation.
- **Response**: `200 OK` (StudentResponse)

---

## 5. Faculty APIs

### 5.1 List & Filter Faculty
- **Endpoint**: `GET /api/admin/faculty`
- **Authorization**: `ROLE_ADMIN`
- **Query Parameters**:
  - `departmentId` (Integer, optional)
  - `search` (String, optional: matches employeeCode, firstName, lastName, or email)
  - `page` (default 0), `size` (default 20), `sort` (default "facultyId,asc")
- **Response**: `200 OK` (PageResponse)

### 5.2 Get Faculty by ID
- **Endpoint**: `GET /api/admin/faculty/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 5.3 Create Faculty
- **Endpoint**: `POST /api/admin/faculty`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "departmentId": 1,
  "employeeCode": "FAC001",
  "firstName": "Dr. Ramesh",
  "lastName": "Kumar",
  "designation": "Associate Professor",
  "specialization": "Machine Learning",
  "email": "ramesh_k@vnrvjiet.in",
  "phone": "9848012345"
}
```
- **Business Rules**:
  - Provisions a `User` identity within the same transaction.
  - Username = email prefix (before `@`)
  - Email = provided email
  - Default password = `Password@123` (BCrypt hashed)
  - Role = `FACULTY`
- **Response**: `201 Created`

### 5.4 Update Faculty
- **Endpoint**: `PUT /api/admin/faculty/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 5.5 Delete Faculty
- **Endpoint**: `DELETE /api/admin/faculty/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Business Rule**: Rejects deletion (`409 Conflict`) if assigned to courses or timetable slots.
- **Response**: `204 No Content`

### 5.6 Faculty Self-Profile
- **Endpoint**: `GET /api/faculty/profile`
- **Authorization**: `ROLE_FACULTY`
- **Response**: `200 OK` (FacultyResponse)

---

## 6. Course APIs

### 6.1 List & Filter Courses
- **Endpoint**: `GET /api/admin/courses`
- **Authorization**: `ROLE_ADMIN`
- **Query Parameters**:
  - `programId` (Integer, optional)
  - `semester` (Integer, optional)
  - `facultyId` (Integer, optional)
  - `search` (String, optional: matches courseCode or courseName)
  - `page` (default 0), `size` (default 20), `sort` (default "courseId,asc")
- **Response**: `200 OK` (PageResponse)

### 6.2 Get Course by ID
- **Endpoint**: `GET /api/admin/courses/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 6.3 Create Course
- **Endpoint**: `POST /api/admin/courses`
- **Authorization**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "programId": 1,
  "facultyId": 2,
  "courseCode": "CS301",
  "courseName": "Operating Systems",
  "credits": 3.0,
  "semester": 5,
  "courseType": "THEORY"
}
```
- **Validation**:
  - `courseCode`: `@NotBlank`, max 20 chars, unique across courses.
  - `courseName`: `@NotBlank`, max 100 chars.
  - `credits`: `@NotNull`, positive (`@DecimalMin(value = "0.5")`).
  - `semester`: `@NotNull`, between 1 and 8 (`@Min(1)`, `@Max(8)`).
  - `courseType`: `@NotNull` (THEORY, LAB, PROJECT, ELECTIVE).
  - `programId`: must exist.
  - `facultyId`: optional (nullable in schema), if provided must exist.
- **Response**: `201 Created`

### 6.4 Update Course
- **Endpoint**: `PUT /api/admin/courses/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Response**: `200 OK`

### 6.5 Delete Course
- **Endpoint**: `DELETE /api/admin/courses/{id}`
- **Authorization**: `ROLE_ADMIN`
- **Business Rule**: Rejects deletion (`409 Conflict`) if referenced by enrollments, attendance, exams, or timetable entries.
- **Response**: `204 No Content`
