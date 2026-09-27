# Phase 6 — Exams and Marks Management API Documentation

This document describes the Exams and Marks/Results Management REST APIs connecting courses, exams, marks, students, and faculty.

---

## 1. Overview & Data Model

- **Entities**:
  - `Exam` (`exams` table): Belongs to `Course` (`course_id`). Has `examName`, `examType`, `examDate`, `maxMarks`.
  - `Mark` (`marks` table): Junction between `Exam` and `Student`. Records `marksObtained`, `grade`, `enteredBy` (`Faculty`).
- **Core Relationships**: `Course` -> `Exam` -> `Mark` -> `Student`
- **Composite Unique Constraint**: `(exam_id, student_id)` on `marks`.
  - A student can have at most one mark entry per exam.
  - Duplicate mark attempts return `HTTP 409 Conflict`.
- **Enrollment Prerequisite**:
  - A student can only receive marks for an exam if the student is actively enrolled in the exam's course.
  - Attempting to enter marks for an unenrolled student returns `HTTP 409 Conflict`.
- **Marks Range Validation**:
  - `marksObtained >= 0` and `marksObtained <= maxMarks`.
  - Violations return `HTTP 400 Bad Request`.
- **Exam Types**: `MID_1`, `MID_2`, `END_SEMESTER`, `LAB`, `INTERNAL`

---

## 2. Security & Ownership Rules

- **Base URL**: `/api`
- **Authentication**: JWT Bearer token required on all endpoints (`Authorization: Bearer <token>`).
- **Role Permissions**:
  - `ROLE_ADMIN`: Full CRUD on exams (`/api/admin/exams/**`) and marks (`/api/admin/marks/**`).
  - `ROLE_FACULTY`:
    - **Exams**: Can only create, view, update, or delete exams for courses assigned to the authenticated faculty member (`/api/faculty/exams/**`).
    - **Marks**: Can only create, view, update, or delete marks for exams belonging to courses assigned to the authenticated faculty member (`/api/faculty/marks/**`).
    - Attempting to manipulate exams or marks for another instructor's course returns `HTTP 403 Forbidden`.
  - `ROLE_STUDENT`:
    - **Exams**: `GET /api/student/exams` — read-only access to exams for courses in which the student is enrolled.
    - **Marks**: `GET /api/student/marks` — read-only access to own marks.
    - **Results**: `GET /api/student/results` — course-level aggregated results and exam breakdown.
    - Identity is derived strictly from JWT (`CustomUserDetails.getUserId()`). Client-supplied `studentId` parameters are never accepted.
    - Modifying or deleting exams/marks returns `HTTP 403 Forbidden`.

---

## 3. Exam Endpoints

### 3.1 Faculty: Manage Exams
- `POST /api/faculty/exams`: Creates an exam for an assigned course.
- `GET /api/faculty/exams`: Lists exams for assigned courses. Supports filters (`courseId`, `examType`, `examDate`, `startDate`, `endDate`) and pagination.
- `PUT /api/faculty/exams/{id}`: Updates an exam for an assigned course.
- `DELETE /api/faculty/exams/{id}`: Deletes an exam. Rejects with `409 Conflict` if marks have already been recorded.

### 3.2 Admin: Manage Exams
- `GET /api/admin/exams`: Lists all exams with optional filters and pagination.
- `GET /api/admin/exams/{id}`: Retrieves single exam or returns `404 Not Found`.
- `POST /api/admin/exams`: Creates an exam for any course.
- `PUT /api/admin/exams/{id}`: Updates an exam.
- `DELETE /api/admin/exams/{id}`: Deletes an exam (rejected if marks exist).

### 3.3 Student: View Enrolled Course Exams
- `GET /api/student/exams`: Returns exams only for courses in which the student is enrolled.

#### Exam Request Payload
```json
{
  "courseId": 1,
  "examName": "Mid Term 1",
  "examType": "MID_1",
  "examDate": "2024-10-15",
  "maxMarks": 30.00
}
```

---

## 4. Marks & Results Endpoints

### 4.1 Faculty: Manage Marks
- `POST /api/faculty/marks`: Records a mark for an exam belonging to an assigned course.
- `GET /api/faculty/marks`: Lists marks for exams in assigned courses. Supports filters (`examId`, `studentId`, `courseId`) and pagination.
- `PUT /api/faculty/marks/{id}`: Updates marks obtained and grade for an assigned course exam.
- `DELETE /api/faculty/marks/{id}`: Deletes a mark entry for an assigned course exam.

### 4.2 Admin: Manage Marks
- `GET /api/admin/marks`: Lists marks across all courses with filters and pagination.
- `GET /api/admin/marks/{id}`: Retrieves single mark record.
- `POST /api/admin/marks`: Records mark for any valid exam.
- `PUT /api/admin/marks/{id}`: Updates mark record.
- `DELETE /api/admin/marks/{id}`: Deletes mark record.

#### Mark Request Payload
```json
{
  "examId": 1,
  "studentId": 1,
  "marksObtained": 27.50,
  "grade": "A+"
}
```
*Note: If `grade` is omitted or empty, it is automatically computed based on standard percentage thresholds (A+: >=90%, A: >=80%, B+: >=70%, B: >=60%, C: >=50%, D: >=40%, F: <40%).*

### 4.3 Student: View Own Marks
- **Endpoint**: `GET /api/student/marks`
- **Authorization**: `ROLE_STUDENT`
- **Query Parameters**: `courseId`, `examId`, `examType`, `semester`, pagination.

### 4.4 Student: View Course Results Summary
- **Endpoint**: `GET /api/student/results`
- **Authorization**: `ROLE_STUDENT`
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Results retrieved successfully",
  "data": [
    {
      "courseId": 1,
      "courseCode": "CS301",
      "courseName": "Operating Systems",
      "totalExams": 2,
      "totalMarksObtained": 55.50,
      "totalMaxMarks": 60.00,
      "percentage": 92.50,
      "examResults": [
        {
          "examId": 1,
          "examName": "Mid Term 1",
          "examType": "MID_1",
          "examDate": "2024-10-15",
          "maxMarks": 30.00,
          "marksObtained": 27.50,
          "grade": "A+"
        },
        {
          "examId": 2,
          "examName": "Mid Term 2",
          "examType": "MID_2",
          "examDate": "2024-11-20",
          "maxMarks": 30.00,
          "marksObtained": 28.00,
          "grade": "A+"
        }
      ]
    }
  ]
}
```
