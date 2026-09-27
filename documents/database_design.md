# Smart Campus Management System — Database Design Specification

**Scope:** Department-level Smart Campus Management System  
**Programs:** AIML, IoT, RAI  
**Database:** MySQL  
**Backend:** Java + Spring Boot + JPA/Hibernate

## 1. Database Goals

The database must support:
- Student and faculty management
- Department and program management
- Course management and enrollment
- Attendance
- Exams and marks/results
- Classrooms and timetable
- Department notices
- Digital document requests and certificate issuance
- Role-based access
- Academic dashboards and reporting

The schema should be normalized, maintain referential integrity, avoid unnecessary duplication, and support meaningful SQL for DBMS evaluation.

## 2. Main Tables

| # | Table | Purpose |
|---|---|---|
| 1 | `users` | Authentication and role information |
| 2 | `departments` | Department information |
| 3 | `programs` | AIML, IoT, RAI programs |
| 4 | `students` | Student academic profile |
| 5 | `faculty` | Faculty profile |
| 6 | `courses` | Courses offered by programs |
| 7 | `enrollments` | Student-course relationship |
| 8 | `attendance` | Student attendance records |
| 9 | `exams` | Examination information |
| 10 | `marks` | Student examination marks |
| 11 | `classrooms` | Classroom/lab information |
| 12 | `timetable` | Scheduled classes |
| 13 | `notices` | Department notices |
| 14 | `document_types` | Supported digital document types |
| 15 | `document_requests` | Student document requests |
| 16 | `document_request_history` | Document status history/audit trail |

## 3. High-Level Relationships

```text
DEPARTMENT
   ├──────────< PROGRAM ──────────< STUDENT
   │                  └──────────< COURSE
   └──────────< FACULTY

STUDENT >──── ENROLLMENT ────< COURSE

STUDENT + COURSE ────────────< ATTENDANCE

COURSE ───────────< EXAM
STUDENT + EXAM ───< MARK

COURSE + PROGRAM + FACULTY + CLASSROOM ───< TIMETABLE

USER ──────────────< NOTICE
PROGRAM ───────────< NOTICE (optional target)

STUDENT ───────────< DOCUMENT_REQUEST >──── DOCUMENT_TYPE
DOCUMENT_REQUEST ──< DOCUMENT_REQUEST_HISTORY
```

The exact cardinalities and constraints will be finalized in the ER diagram.

## 4. `users`

| Column | Type | Constraint |
|---|---|---|
| `user_id` | BIGINT | PK |
| `username` | VARCHAR(100) | UNIQUE, NOT NULL |
| `password_hash` | VARCHAR(255) | NOT NULL |
| `role` | ENUM | NOT NULL: ADMIN/FACULTY/STUDENT |
| `email` | VARCHAR(150) | UNIQUE, NOT NULL |
| `is_active` | BOOLEAN | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL |

Passwords must never be stored in plain text.

## 5. `departments`

| Column | Type | Constraint |
|---|---|---|
| `department_id` | BIGINT | PK |
| `department_code` | VARCHAR(20) | UNIQUE, NOT NULL |
| `department_name` | VARCHAR(150) | NOT NULL |
| `description` | TEXT | NULL |
| `created_at` | TIMESTAMP | NOT NULL |

## 6. `programs`

| Column | Type | Constraint |
|---|---|---|
| `program_id` | BIGINT | PK |
| `department_id` | BIGINT | FK → departments |
| `program_code` | VARCHAR(20) | UNIQUE, NOT NULL |
| `program_name` | VARCHAR(150) | NOT NULL |
| `duration_years` | INT | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |

Initial programs:
- AIML
- IOT
- RAI

Relationship: `Department 1 ────< Programs`

## 7. `students`

| Column | Type | Constraint |
|---|---|---|
| `student_id` | BIGINT | PK |
| `user_id` | BIGINT | FK → users, UNIQUE |
| `program_id` | BIGINT | FK → programs |
| `roll_number` | VARCHAR(50) | UNIQUE, NOT NULL |
| `first_name` | VARCHAR(100) | NOT NULL |
| `last_name` | VARCHAR(100) | NULL |
| `date_of_birth` | DATE | NULL |
| `gender` | VARCHAR(20) | NULL |
| `admission_year` | INT | NOT NULL |
| `current_semester` | INT | NOT NULL |
| `phone` | VARCHAR(20) | NULL |
| `created_at` | TIMESTAMP | NOT NULL |

## 8. `faculty`

| Column | Type | Constraint |
|---|---|---|
| `faculty_id` | BIGINT | PK |
| `user_id` | BIGINT | FK → users, UNIQUE |
| `department_id` | BIGINT | FK → departments |
| `employee_code` | VARCHAR(50) | UNIQUE, NOT NULL |
| `first_name` | VARCHAR(100) | NOT NULL |
| `last_name` | VARCHAR(100) | NULL |
| `designation` | VARCHAR(100) | NULL |
| `specialization` | VARCHAR(150) | NULL |
| `phone` | VARCHAR(20) | NULL |
| `created_at` | TIMESTAMP | NOT NULL |

## 9. `courses`

| Column | Type | Constraint |
|---|---|---|
| `course_id` | BIGINT | PK |
| `program_id` | BIGINT | FK → programs |
| `faculty_id` | BIGINT | FK → faculty, NULL allowed |
| `course_code` | VARCHAR(30) | UNIQUE, NOT NULL |
| `course_name` | VARCHAR(150) | NOT NULL |
| `credits` | DECIMAL(3,1) | NOT NULL |
| `semester` | INT | NOT NULL |
| `course_type` | VARCHAR(30) | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |

Possible course types: `THEORY`, `LAB`, `PROJECT`, `ELECTIVE`.

## 10. `enrollments`

Resolves the many-to-many relationship between students and courses.

| Column | Type | Constraint |
|---|---|---|
| `enrollment_id` | BIGINT | PK |
| `student_id` | BIGINT | FK → students |
| `course_id` | BIGINT | FK → courses |
| `academic_year` | VARCHAR(20) | NOT NULL |
| `semester` | INT | NOT NULL |
| `enrollment_date` | DATE | NOT NULL |
| `status` | VARCHAR(30) | NOT NULL |

Recommended unique constraint:
`(student_id, course_id, academic_year, semester)`

## 11. `attendance`

| Column | Type | Constraint |
|---|---|---|
| `attendance_id` | BIGINT | PK |
| `student_id` | BIGINT | FK → students |
| `course_id` | BIGINT | FK → courses |
| `attendance_date` | DATE | NOT NULL |
| `status` | ENUM | PRESENT/ABSENT/LATE |
| `marked_by` | BIGINT | FK → faculty |
| `created_at` | TIMESTAMP | NOT NULL |

Recommended unique constraint:
`(student_id, course_id, attendance_date)`

## 12. `exams`

| Column | Type | Constraint |
|---|---|---|
| `exam_id` | BIGINT | PK |
| `course_id` | BIGINT | FK → courses |
| `exam_name` | VARCHAR(100) | NOT NULL |
| `exam_type` | VARCHAR(50) | NOT NULL |
| `exam_date` | DATE | NOT NULL |
| `max_marks` | DECIMAL(5,2) | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |

Examples: `MID_1`, `MID_2`, `END_SEMESTER`, `LAB`, `INTERNAL`.

## 13. `marks`

| Column | Type | Constraint |
|---|---|---|
| `mark_id` | BIGINT | PK |
| `exam_id` | BIGINT | FK → exams |
| `student_id` | BIGINT | FK → students |
| `marks_obtained` | DECIMAL(5,2) | NOT NULL |
| `grade` | VARCHAR(5) | NULL |
| `entered_by` | BIGINT | FK → faculty |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL |

Recommended unique constraint: `(exam_id, student_id)`.

## 14. `classrooms`

| Column | Type | Constraint |
|---|---|---|
| `classroom_id` | BIGINT | PK |
| `room_number` | VARCHAR(30) | UNIQUE, NOT NULL |
| `building` | VARCHAR(100) | NOT NULL |
| `room_type` | VARCHAR(50) | NOT NULL |
| `capacity` | INT | NOT NULL |
| `is_active` | BOOLEAN | NOT NULL |

Possible room types: `CLASSROOM`, `LAB`, `SEMINAR_HALL`.

## 15. `timetable`

| Column | Type | Constraint |
|---|---|---|
| `timetable_id` | BIGINT | PK |
| `program_id` | BIGINT | FK → programs |
| `course_id` | BIGINT | FK → courses |
| `faculty_id` | BIGINT | FK → faculty |
| `classroom_id` | BIGINT | FK → classrooms |
| `day_of_week` | VARCHAR(15) | NOT NULL |
| `start_time` | TIME | NOT NULL |
| `end_time` | TIME | NOT NULL |
| `semester` | INT | NOT NULL |
| `academic_year` | VARCHAR(20) | NOT NULL |

The application should prevent faculty, classroom, and program scheduling conflicts.

## 16. `notices`

| Column | Type | Constraint |
|---|---|---|
| `notice_id` | BIGINT | PK |
| `title` | VARCHAR(200) | NOT NULL |
| `content` | TEXT | NOT NULL |
| `category` | VARCHAR(50) | NOT NULL |
| `priority` | VARCHAR(20) | NOT NULL |
| `target_program_id` | BIGINT | FK → programs, NULL |
| `published_by` | BIGINT | FK → users |
| `publish_at` | TIMESTAMP | NOT NULL |
| `expires_at` | TIMESTAMP | NULL |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL |

Categories can include Academic, Exam, Event, Internship, Placement, Important, and General.

A null target program can represent a department-wide notice.

## 17. `document_types`

| Column | Type | Constraint |
|---|---|---|
| `document_type_id` | BIGINT | PK |
| `document_name` | VARCHAR(100) | UNIQUE, NOT NULL |
| `description` | TEXT | NULL |
| `requires_approval` | BOOLEAN | NOT NULL |
| `is_active` | BOOLEAN | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |

Initial document type: `BONAFIDE_CERTIFICATE`.

This table allows future document types without changing the request schema.

## 18. `document_requests`

| Column | Type | Constraint |
|---|---|---|
| `request_id` | BIGINT | PK |
| `request_number` | VARCHAR(50) | UNIQUE, NOT NULL |
| `student_id` | BIGINT | FK → students |
| `document_type_id` | BIGINT | FK → document_types |
| `purpose` | VARCHAR(255) | NOT NULL |
| `additional_details` | TEXT | NULL |
| `status` | VARCHAR(30) | NOT NULL |
| `reviewed_by` | BIGINT | FK → users, NULL |
| `submitted_at` | TIMESTAMP | NOT NULL |
| `reviewed_at` | TIMESTAMP | NULL |
| `issued_at` | TIMESTAMP | NULL |
| `document_path` | VARCHAR(500) | NULL |
| `verification_code` | VARCHAR(100) | UNIQUE, NULL |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL |

Status flow:

```text
SUBMITTED → UNDER_REVIEW → APPROVED → ISSUED
                    │
                    └──────────────→ REJECTED
```

## 19. `document_request_history`

Provides an audit trail for document requests.

| Column | Type | Constraint |
|---|---|---|
| `history_id` | BIGINT | PK |
| `request_id` | BIGINT | FK → document_requests |
| `old_status` | VARCHAR(30) | NULL |
| `new_status` | VARCHAR(30) | NOT NULL |
| `changed_by` | BIGINT | FK → users |
| `remarks` | TEXT | NULL |
| `changed_at` | TIMESTAMP | NOT NULL |

## 20. Core Constraints

### Primary keys
Every table has a primary key.

### Foreign keys
All relationships must use foreign keys to maintain referential integrity.

### Important unique fields
- `users.username`
- `users.email`
- `students.roll_number`
- `faculty.employee_code`
- `courses.course_code`
- `classrooms.room_number`
- `document_requests.request_number`
- `document_requests.verification_code`

### Validation/check rules
Where appropriate:
- credits > 0
- capacity > 0
- marks_obtained >= 0
- max_marks > 0
- start_time < end_time

## 21. Normalization

The schema should target **Third Normal Form (3NF)** where practical.

Examples:
- Department data is stored once in `departments`.
- Program data is stored once in `programs`.
- Student-course many-to-many data is represented by `enrollments`.
- Document types are separated from document requests.
- Request history is separated from the current request status.

Avoid storing duplicated or unnecessarily derived data.

## 22. Indexing Strategy

Potential indexes should be considered for:
- `students.program_id`
- `faculty.department_id`
- `courses.program_id`
- `courses.faculty_id`
- `enrollments.student_id`
- `enrollments.course_id`
- `attendance.student_id`
- `attendance.course_id`
- `attendance.attendance_date`
- `exams.course_id`
- `marks.student_id`
- `marks.exam_id`
- `timetable.program_id`
- `timetable.faculty_id`
- `notices.target_program_id`
- `notices.publish_at`
- `document_requests.student_id`
- `document_requests.status`
- `document_request_history.request_id`

Indexes should be justified by query patterns.

## 23. DBMS Queries to Demonstrate

The project should include meaningful SQL such as:

### Attendance percentage

```sql
SELECT
    student_id,
    course_id,
    (SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0
     / COUNT(*)) AS attendance_percentage
FROM attendance
GROUP BY student_id, course_id;
```

### Student course list

```sql
SELECT
    s.roll_number,
    s.first_name,
    c.course_code,
    c.course_name
FROM students s
JOIN enrollments e ON s.student_id = e.student_id
JOIN courses c ON e.course_id = c.course_id
WHERE s.student_id = ?;
```

### Average marks

```sql
SELECT
    student_id,
    AVG(marks_obtained) AS average_marks
FROM marks
GROUP BY student_id;
```

### Pending document requests

```sql
SELECT
    dr.request_number,
    s.roll_number,
    s.first_name,
    dt.document_name,
    dr.status,
    dr.submitted_at
FROM document_requests dr
JOIN students s ON dr.student_id = s.student_id
JOIN document_types dt ON dr.document_type_id = dt.document_type_id
WHERE dr.status IN ('SUBMITTED', 'UNDER_REVIEW')
ORDER BY dr.submitted_at;
```

## 24. DBMS Concepts Demonstrated

The project should explicitly demonstrate:
- Relational database design
- ER modeling
- Primary and foreign keys
- Candidate/unique keys
- Constraints
- Normalization
- One-to-one, one-to-many, and many-to-many relationships
- SQL joins
- Aggregate functions
- GROUP BY / HAVING
- Subqueries
- Indexing
- Transactions
- Referential integrity
- CRUD
- Database security
- Audit/history records

Triggers, stored procedures, and views should be added only where they have a genuine business purpose.

## 25. Transaction Example — Digital Document Approval

```text
BEGIN TRANSACTION

1. Validate request
2. Update request status
3. Insert status history
4. Generate/associate certificate
5. Store verification information

COMMIT
```

If a required operation fails, related database changes should roll back.

## 26. Future Extension Compatibility

The schema should allow future modules such as:
- Additional digital document types
- More notice categories
- Additional programs
- Department events
- Feedback
- Placement information
- Other department services

These should be modular extensions rather than reasons to redesign the entire database.

## 27. ER Diagram Requirement

A final ER diagram must be produced from this specification and kept consistent with the actual MySQL implementation.

It must show:
- Entities
- Primary keys
- Foreign keys
- Cardinalities
- One-to-one relationships
- One-to-many relationships
- Many-to-many relationships

## 28. Design Principle

The database is the foundation of the system.

Every backend feature should map to a clear database model, and every database relationship should have a clear business purpose.

**Correctness → Integrity → Maintainability → Performance → Extensibility**

---

**Status:** Database Design Draft v1.0

This document is the initial database specification. Schema changes should be reviewed before implementation.
