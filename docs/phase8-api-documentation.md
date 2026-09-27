# Phase 8 — Smart Department Notice Centre API Documentation

This document describes the Department Notice Centre REST APIs for ADMIN, FACULTY, and STUDENT roles.

---

## 1. Overview & Data Model

- **Entities**:
  - `Notice` (`notices` table): Contains `noticeId`, `title`, `content`, `category`, `priority`, `targetProgram`, `publishedBy`, `publishAt`, `expiresAt`, `createdAt`, `updatedAt`.
  - `NoticeCategory` (Enum): `ACADEMIC`, `EXAM`, `EVENT`, `INTERNSHIP`, `PLACEMENT`, `IMPORTANT`, `GENERAL`.
  - `NoticePriority` (Enum): `NORMAL`, `HIGH`, `URGENT`.
- **Targeting Model**:
  - `targetProgram = null`: Department-wide notice (visible to all students within the department).
  - `targetProgram = <Program>`: Program-specific notice (visible only to students enrolled in that program).
- **Active Notice Rules**:
  A notice is considered active if:
  $$\text{publishAt} \le \text{currentTime} \quad\text{AND}\quad (\text{expiresAt IS NULL} \;\lor\; \text{expiresAt} \ge \text{currentTime})$$
  - Future notices (`publishAt > currentTime`) are NOT visible to students.
  - Expired notices (`expiresAt < currentTime`) are NOT visible to students.

---

## 2. Security & Role Permissions

- **Base URL**: `/api`
- **Authentication**: JWT Bearer token required on all endpoints (`Authorization: Bearer <token>`).
- **Role Permissions**:
  - `ROLE_ADMIN`:
    - Full CRUD access to all notices (`/api/admin/notices/**`).
    - Can create, update, or delete any notice regardless of creator.
  - `ROLE_FACULTY`:
    - Can create notices (`POST /api/faculty/notices`).
    - Can view notices (`GET /api/faculty/notices`, `GET /api/faculty/notices/{id}`).
    - **Faculty Ownership Rule**: Can update or delete **ONLY** notices published by that faculty member (`Notice.publishedBy.userId == authenticatedUserId`). Attempting to update or delete another faculty member's notice returns `HTTP 403 Forbidden`.
  - `ROLE_STUDENT`:
    - Read-only access to relevant, active notices (`GET /api/student/notices`, `GET /api/student/notices/{id}`).
    - **Program Visibility Rule**: Students only receive notices targeted to their enrolled program or department-wide notices (`targetProgram IS NULL`).
    - Future notices and expired notices are never returned in student queries.
    - Students cannot bypass visibility by requesting notice IDs directly (`GET /api/student/notices/{id}` for another program's notice or an inactive notice returns `HTTP 403 Forbidden`).
    - Accessing `/api/admin/notices/**` or `/api/faculty/notices/**` returns `HTTP 403 Forbidden`.

---

## 3. Endpoints

### 3.1 Admin Notice Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/notices` | List all notices with filtering (`category`, `priority`, `keyword`, `active`) and pagination. |
| `GET` | `/api/admin/notices/{noticeId}` | Get notice by ID. |
| `POST` | `/api/admin/notices` | Create a new notice. |
| `PUT` | `/api/admin/notices/{noticeId}` | Update any notice. |
| `DELETE` | `/api/admin/notices/{noticeId}` | Delete any notice. |

### 3.2 Faculty Notice Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/faculty/notices` | List notices with filtering (`category`, `priority`, `keyword`, `active`) and pagination. |
| `GET` | `/api/faculty/notices/{noticeId}` | Get notice by ID. |
| `POST` | `/api/faculty/notices` | Create a new notice. |
| `PUT` | `/api/faculty/notices/{noticeId}` | Update notice owned by authenticated faculty member (`403 Forbidden` if owned by someone else). |
| `DELETE` | `/api/faculty/notices/{noticeId}` | Delete notice owned by authenticated faculty member (`403 Forbidden` if owned by someone else). |

### 3.3 Student Notice Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/student/notices` | List active notices relevant to student's program and department-wide notices. Supports filters (`category`, `priority`, `keyword`) and pagination (`publishAt DESC`). |
| `GET` | `/api/student/notices/{noticeId}` | Get a specific notice if it is currently active and relevant to the student (`403 Forbidden` otherwise). |

---

## 4. Request & Response Formats

### 4.1 Notice Request Payload
```json
{
  "title": "AIML Mid Term Examination Schedule",
  "content": "Mid-term examination schedule for Semester 3 has been released.",
  "category": "EXAM",
  "priority": "URGENT",
  "targetProgramId": 1,
  "publishAt": "2024-09-28T09:00:00",
  "expiresAt": "2024-10-05T23:59:59"
}
```

*Validation Rules:*
- `title`: Required, max 200 characters.
- `content`: Required.
- `category`: Required (`ACADEMIC`, `EXAM`, `EVENT`, `INTERNSHIP`, `PLACEMENT`, `IMPORTANT`, `GENERAL`).
- `priority`: Required (`NORMAL`, `HIGH`, `URGENT`).
- `publishAt`: Required (`LocalDateTime`).
- `targetProgramId`: Optional (null = department-wide). Must reference a valid Program ID if provided (`404 Not Found` if missing).
- `expiresAt`: Optional (`LocalDateTime`). Cannot be before `publishAt` (`400 Bad Request` if invalid).

### 4.2 Notice Response Payload
```json
{
  "success": true,
  "message": "Notice retrieved successfully",
  "data": {
    "noticeId": 1,
    "title": "AIML Mid Term Examination Schedule",
    "content": "Mid-term examination schedule for Semester 3 has been released.",
    "category": "EXAM",
    "priority": "URGENT",
    "targetProgramId": 1,
    "targetProgramCode": "AIML",
    "targetProgramName": "B.Tech in Artificial Intelligence & Machine Learning",
    "publishedByUserId": 5,
    "publishedByName": "amita.garg",
    "publishAt": "2024-09-28T09:00:00",
    "expiresAt": "2024-10-05T23:59:59",
    "createdAt": "2024-09-28T08:50:00",
    "updatedAt": "2024-09-28T08:50:00",
    "active": true
  }
}
```

---

## 5. HTTP Status Codes & Error Handling

| Status Code | Scenario |
|---|---|
| `200 OK` | Successful retrieval, update, or deletion. |
| `400 Bad Request` | Validation failure (e.g. `expiresAt < publishAt`, missing required fields). |
| `401 Unauthorized` | Missing, invalid, or expired JWT Bearer token. |
| `403 Forbidden` | Access denied (e.g. student accessing admin endpoints, faculty modifying another faculty's notice, student requesting a non-relevant notice). |
| `404 Not Found` | Entity not found (notice ID or target program ID does not exist). |
