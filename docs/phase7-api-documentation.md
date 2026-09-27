# Phase 7 — Classrooms and Timetable Management API Documentation

This document describes the Classroom Management and Timetable Scheduling REST APIs connecting classrooms, timetable slots, programs, courses, faculty, and students.

---

## 1. Overview & Data Model

- **Entities**:
  - `Classroom` (`classrooms` table): Physical room or lab with `roomNumber`, `building`, `roomType` (`CLASSROOM`, `LAB`, `SEMINAR_HALL`), `capacity`, `isActive`.
  - `Timetable` (`timetable` table): Scheduled class slot linking `Program`, `Course`, `Faculty`, `Classroom`, `dayOfWeek`, `startTime`, `endTime`, `semester`, and `academicYear`.
- **Classroom Constraints**:
  - `roomNumber` must be unique across all classrooms (`409 Conflict` on duplicate).
  - `capacity` must be strictly positive (`capacity > 0`, `400 Bad Request` if invalid).
  - Deletion is prevented if referenced by existing timetable entries (`409 Conflict`).
- **Timetable Constraints & Conflict Detection**:
  - `startTime` must be strictly before `endTime` (`400 Bad Request`).
  - Referenced entities (`program`, `course`, `faculty`, `classroom`) must exist (`404 Not Found`).
  - Classroom must be active (`409 Conflict` if inactive).
  - Course must belong to the specified program (`409 Conflict` on mismatch).
  - **Classroom Clash**: The same classroom cannot have overlapping slots on the same day, semester, and academic year (`409 Conflict`).
  - **Faculty Clash**: The same faculty member cannot have overlapping slots on the same day, semester, and academic year (`409 Conflict`).
  - **Program Clash**: The same program cannot have overlapping slots on the same day, semester, and academic year (`409 Conflict`).
  - **Time Overlap Condition**:
    `existing.startTime < requested.endTime AND existing.endTime > requested.startTime`
  - **Adjacent Classes Allowed**: Boundary-touching slots (e.g., 09:00–10:00 and 10:00–11:00) do NOT overlap and are permitted.
  - **Self-Conflict Exclusion**: Update operations exclude the current record ID during overlap checks.

---

## 2. Security & Role Permissions

- **Base URL**: `/api`
- **Authentication**: JWT Bearer token required on all endpoints (`Authorization: Bearer <token>`).
- **Role Permissions**:
  - `ROLE_ADMIN`:
    - Full CRUD on classrooms (`/api/admin/classrooms/**`).
    - Full CRUD on timetable (`/api/admin/timetable/**`).
  - `ROLE_FACULTY`:
    - Read-only access to own timetable (`GET /api/faculty/timetable`). Identity derived from JWT.
    - Modifying or accessing classroom endpoints returns `403 Forbidden`.
    - Modifying or accessing admin timetable endpoints returns `403 Forbidden`.
  - `ROLE_STUDENT`:
    - Read-only access to program timetable (`GET /api/student/timetable`). Identity derived from JWT.
    - Modifying or accessing classroom endpoints returns `403 Forbidden`.
    - Modifying or accessing admin timetable endpoints returns `403 Forbidden`.

---

## 3. Classroom Endpoints (Admin Only)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/classrooms` | List all classrooms with filters (`roomNumber`, `building`, `roomType`, `active`) and pagination. |
| `GET` | `/api/admin/classrooms/{id}` | Get classroom details by ID. |
| `POST` | `/api/admin/classrooms` | Create a new classroom. |
| `PUT` | `/api/admin/classrooms/{id}` | Update an existing classroom. |
| `DELETE` | `/api/admin/classrooms/{id}` | Delete classroom (prevented if scheduled in timetable). |

### 3.1 Classroom Request Payload
```json
{
  "roomNumber": "LH-101",
  "building": "Aryabhatta Block",
  "roomType": "CLASSROOM",
  "capacity": 75,
  "isActive": true
}
```

### 3.2 Classroom Response Payload
```json
{
  "success": true,
  "message": "Classroom retrieved successfully",
  "data": {
    "classroomId": 1,
    "roomNumber": "LH-101",
    "building": "Aryabhatta Block",
    "roomType": "CLASSROOM",
    "capacity": 75,
    "isActive": true
  }
}
```

---

## 4. Timetable Endpoints

### 4.1 Admin: Full CRUD
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/timetable` | List timetable entries with filters (`programId`, `courseId`, `facultyId`, `classroomId`, `dayOfWeek`, `semester`, `academicYear`) and pagination. |
| `GET` | `/api/admin/timetable/{id}` | Get timetable entry by ID. |
| `POST` | `/api/admin/timetable` | Schedule a new class slot with conflict validation. |
| `PUT` | `/api/admin/timetable/{id}` | Update a class slot with conflict validation (excluding self). |
| `DELETE` | `/api/admin/timetable/{id}` | Delete a timetable entry. |

### 4.2 Faculty: View Schedule
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/faculty/timetable` | Read-only timetable for authenticated faculty member with optional filters (`dayOfWeek`, `semester`, `academicYear`). |

### 4.3 Student: View Schedule
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/student/timetable` | Read-only timetable for authenticated student's program and semester with optional filters (`dayOfWeek`, `semester`, `academicYear`). |

### 4.4 Timetable Request Payload
```json
{
  "programId": 1,
  "courseId": 1,
  "facultyId": 5,
  "classroomId": 1,
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "semester": 3,
  "academicYear": "2024-2025"
}
```

### 4.5 Timetable Response Payload
```json
{
  "success": true,
  "message": "Timetable entry created successfully",
  "data": {
    "timetableId": 1,
    "programId": 1,
    "programCode": "AIML",
    "programName": "B.Tech in Artificial Intelligence & Machine Learning",
    "courseId": 1,
    "courseCode": "CS301",
    "courseName": "Data Structures",
    "facultyId": 5,
    "facultyName": "Dr. Amita Garg",
    "facultyEmployeeCode": "FAC005",
    "classroomId": 1,
    "roomNumber": "LH-101",
    "building": "Aryabhatta Tech Block",
    "roomType": "CLASSROOM",
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "semester": 3,
    "academicYear": "2024-2025"
  }
}
```

---

## 5. HTTP Status Codes & Error Handling

| Status Code | Scenario |
|---|---|
| `200 OK` | Successful retrieval, update, or deletion. |
| `201 Created` | Successful creation of classroom or timetable entry. |
| `400 Bad Request` | Validation failure (e.g. `startTime >= endTime`, capacity <= 0, invalid day of week). |
| `401 Unauthorized` | Missing, malformed, or expired JWT token. |
| `403 Forbidden` | Insufficient permissions (e.g. faculty/student modifying classrooms or timetable). |
| `404 Not Found` | Entity does not exist (classroom, timetable, program, course, faculty). |
| `409 Conflict` | Duplicate room number, active timetable clash (classroom, faculty, or program), deleting classroom with active slots. |
