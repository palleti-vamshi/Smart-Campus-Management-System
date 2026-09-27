# Phase 9: Digital Document & Certificate Service API Documentation

## 1. Overview
The **Digital Document & Certificate Service** provides a secure, streamlined, department-level platform for students to request official academic documents (e.g., Bonafide Certificates, Transcripts, Internship NOCs) and for administrators to review, approve, reject, issue, and cryptographically stamp them with verification codes.

Upon document issuance (`APPROVED → ISSUED`), the system dynamically generates a tamper-evident digital certificate (PDF format) stored securely in configurable local storage and provides a public verification endpoint for external authorities (banks, companies, transit authorities).

---

## 2. Document Lifecycle Workflow

```
Student Creates Request
       ↓
  [SUBMITTED] ──(Admin Rejects)──> [REJECTED] (Terminal)
       ↓
(Admin Moves to Review)
       ↓
 [UNDER_REVIEW] ──(Admin Rejects)──> [REJECTED] (Terminal)
       ↓
(Admin Approves)
       ↓
   [APPROVED]
       ↓
 (Admin Issues)
       ↓
    [ISSUED] (Terminal)
       ├── Unique Verification Code Generated (SCMS-YYYY-XXXXXX)
       ├── Digital Certificate PDF Rendered & Stored
       └── Available for Student/Admin Download & Public Verification
```

---

## 3. Status Transition Matrix

| Current Status | Target Status | Allowed? | Notes |
|---|---|---|---|
| `SUBMITTED` | `UNDER_REVIEW` | Yes | Begins administrative examination |
| `SUBMITTED` | `REJECTED` | Yes | Reject directly with remarks |
| `SUBMITTED` | `APPROVED` | **NO** | Must go through `UNDER_REVIEW` first |
| `SUBMITTED` | `ISSUED` | **NO** | Illegal transition jump |
| `UNDER_REVIEW` | `APPROVED` | Yes | Validated and approved for issuance |
| `UNDER_REVIEW` | `REJECTED` | Yes | Rejected with remarks |
| `UNDER_REVIEW` | `ISSUED` | **NO** | Must be approved first |
| `APPROVED` | `ISSUED` | Yes | Generates digital PDF and verification code |
| `APPROVED` | `REJECTED` | **NO** | Not allowed once approved |
| `ISSUED` | *Any* | **NO** | Terminal state |
| `REJECTED` | *Any* | **NO** | Terminal state |

*Any unauthorized or illegal status transition returns HTTP 400 Bad Request (`InvalidOperationException`).*

---

## 4. Role-Based Access Control

- **ADMIN (`ROLE_ADMIN`)**:
  - Full CRUD on document types (`/api/admin/document-types`).
  - Search, filter, and paginate all document requests across all students (`/api/admin/document-requests`).
  - Review and update request statuses (`SUBMITTED → UNDER_REVIEW → APPROVED → ISSUED` or `REJECTED`).
  - Access audit trails and download issued certificate PDFs.
- **STUDENT (`ROLE_STUDENT`)**:
  - View all active document types (`/api/student/document-types`).
  - Create new document requests (`/api/student/document-requests`).
  - List and inspect only their own document requests and request histories.
  - Download only their own issued certificate PDFs.
  - Forbidden from inspecting or modifying other students' requests (enforced via authenticated JWT identity).
- **FACULTY (`ROLE_FACULTY`)**:
  - Restricted from administrative document management.
- **PUBLIC / UNAUTHENTICATED**:
  - Verify issued documents via `/api/public/documents/verify/{verificationCode}` without authentication.

---

## 5. Document Type APIs (Admin & Student)

### 5.1 Admin: Create Document Type
- **Endpoint**: `POST /api/admin/document-types`
- **Security**: Admin only (`ROLE_ADMIN`)
- **Request Body**:
```json
{
  "documentName": "INTERNSHIP_NOC",
  "description": "No Objection Certificate for industrial internship participation",
  "requiresApproval": true,
  "isActive": true
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Document type created successfully",
  "data": {
    "documentTypeId": 5,
    "documentName": "INTERNSHIP_NOC",
    "description": "No Objection Certificate for industrial internship participation",
    "requiresApproval": true,
    "isActive": true,
    "createdAt": "2026-09-28T02:00:00"
  }
}
```

### 5.2 Admin: List All Document Types
- **Endpoint**: `GET /api/admin/document-types`
- **Security**: Admin only (`ROLE_ADMIN`)
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Document types retrieved successfully",
  "data": [
    {
      "documentTypeId": 1,
      "documentName": "BONAFIDE_CERTIFICATE",
      "description": "Certifies regular student status",
      "requiresApproval": true,
      "isActive": true,
      "createdAt": "2026-09-28T01:00:00"
    }
  ]
}
```

### 5.3 Admin: Update Document Type
- **Endpoint**: `PUT /api/admin/document-types/{documentTypeId}`
- **Security**: Admin only (`ROLE_ADMIN`)

### 5.4 Admin: Delete Document Type
- **Endpoint**: `DELETE /api/admin/document-types/{documentTypeId}`
- **Security**: Admin only (`ROLE_ADMIN`)
- *Note: Blocked with HTTP 409 Conflict if referenced by existing document requests.*

### 5.5 Student: List Active Document Types
- **Endpoint**: `GET /api/student/document-types`
- **Security**: Student only (`ROLE_STUDENT`)
- **Response (200 OK)**: Returns list of document types where `isActive = true`.

---

## 6. Student Request APIs

### 6.1 Create Document Request
- **Endpoint**: `POST /api/student/document-requests`
- **Security**: Student only (`ROLE_STUDENT`)
- **Request Body**:
```json
{
  "documentTypeId": 1,
  "purpose": "State scholarship application and bank loan verification",
  "additionalDetails": "Submitted for State Bank of India branch verification"
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Document request submitted successfully",
  "data": {
    "requestId": 12,
    "requestNumber": "DOC-2026-482910",
    "studentId": 1,
    "studentRollNumber": "23CSE001",
    "studentName": "Alice Smith",
    "programId": 1,
    "programName": "B.Tech in Computer Science & Engineering",
    "documentTypeId": 1,
    "documentTypeName": "BONAFIDE_CERTIFICATE",
    "purpose": "State scholarship application and bank loan verification",
    "additionalDetails": "Submitted for State Bank of India branch verification",
    "status": "SUBMITTED",
    "reviewedByUserId": null,
    "reviewedByUsername": null,
    "submittedAt": "2026-09-28T02:05:00",
    "reviewedAt": null,
    "issuedAt": null,
    "documentPath": null,
    "verificationCode": null,
    "createdAt": "2026-09-28T02:05:00",
    "updatedAt": "2026-09-28T02:05:00"
  }
}
```

### 6.2 List Own Requests
- **Endpoint**: `GET /api/student/document-requests?page=0&size=20&sort=requestId,desc`
- **Security**: Student only (`ROLE_STUDENT`)

### 6.3 Get Own Request by ID
- **Endpoint**: `GET /api/student/document-requests/{requestId}`
- **Security**: Student only (`ROLE_STUDENT`)
- *Returns 403 Forbidden if the request belongs to another student.*

### 6.4 Get Own Request History
- **Endpoint**: `GET /api/student/document-requests/{requestId}/history`
- **Security**: Student only (`ROLE_STUDENT`)
- *Returns chronological list of status changes with remarks and timestamps.*

### 6.5 Download Own Issued Document
- **Endpoint**: `GET /api/student/document-requests/{requestId}/download`
- **Security**: Student only (`ROLE_STUDENT`)
- **Headers**:
  - `Content-Type: application/pdf`
  - `Content-Disposition: attachment; filename="CERT-DOC-2026-482910.pdf"`
- *Returns 400 Bad Request if document is not in `ISSUED` status.*
- *Returns 403 Forbidden if accessed by another student.*

---

## 7. Admin Review & Management APIs

### 7.1 Filter and Search All Document Requests
- **Endpoint**: `GET /api/admin/document-requests`
- **Parameters**:
  - `status` (optional): `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `ISSUED`, `REJECTED`
  - `documentTypeId` (optional): Filter by document type ID
  - `search` (optional): Matches request number, student roll number, or student first/last name
  - `page`, `size`, `sort`
- **Response (200 OK)**: Paginated `PageResponse<DocumentRequestResponse>`.

### 7.2 Get Request Details
- **Endpoint**: `GET /api/admin/document-requests/{requestId}`
- **Security**: Admin only (`ROLE_ADMIN`)

### 7.3 Get Request History
- **Endpoint**: `GET /api/admin/document-requests/{requestId}/history`
- **Security**: Admin only (`ROLE_ADMIN`)

### 7.4 Update Request Status (Review / Approve / Issue / Reject)
- **Endpoint**: `PUT /api/admin/document-requests/{requestId}/status`
- **Security**: Admin only (`ROLE_ADMIN`)
- **Request Body**:
```json
{
  "status": "UNDER_REVIEW",
  "remarks": "Academic record and enrollment verified"
}
```
- **Response (200 OK)**: Returns updated `DocumentRequestResponse`.
- When updating to `ISSUED`:
  - `verificationCode` is automatically generated (`SCMS-YYYY-XXXXXX`).
  - Digital certificate PDF is generated and saved to the document repository.
  - `documentPath` is assigned.
  - `issuedAt` is recorded.
  - History row is created transactionally.

### 7.5 Admin Download Issued Document
- **Endpoint**: `GET /api/admin/document-requests/{requestId}/download`
- **Security**: Admin only (`ROLE_ADMIN`)
- *Returns PDF document for any issued certificate.*

---

## 8. Public Document Verification API

- **Endpoint**: `GET /api/public/documents/verify/{verificationCode}`
- **Authentication**: None (Publicly accessible)
- **Security Considerations**: Never exposes student email, phone, database IDs, or private internal notes.
- **Response (200 OK - Valid Certificate)**:
```json
{
  "success": true,
  "message": "Document verified successfully",
  "data": {
    "valid": true,
    "requestNumber": "DOC-2026-482910",
    "documentType": "BONAFIDE_CERTIFICATE",
    "studentName": "Alice Smith",
    "program": "B.Tech in Computer Science & Engineering",
    "issueDate": "2026-09-28T02:15:30",
    "status": "ISSUED"
  }
}
```
- **Response (404 Not Found - Invalid Code)**:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No document found with verification code: INVALID-CODE",
  "path": "/api/public/documents/verify/INVALID-CODE"
}
```
- **Response (400 Bad Request - Non-Issued/Rejected Document)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Document is not in ISSUED status and cannot be verified",
  "path": "/api/public/documents/verify/REJECTED-CODE"
}
```

---

## 9. Storage Configuration & Security

- **Storage Location**: Configured via application property `document.storage.path` (defaults to `./storage/documents`).
- **Path Traversal Protection**: All download and generation routines resolve normalized canonical paths and assert `targetPath.startsWith(storageDirectory)`, preventing path traversal attacks (`../`).
- **Atomicity**: Document generation occurs within the service transition logic; if file generation encounters an error, the database transaction rolls back, ensuring no orphaned `ISSUED` records without corresponding files.
