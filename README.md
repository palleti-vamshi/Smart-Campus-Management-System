# Smart Campus Management System

A department-level academic management platform engineered for academic administration and modern DBMS evaluation.

Designed for specialized engineering programs:
- **AIML** — Artificial Intelligence & Machine Learning
- **IOT** — Internet of Things
- **RAI** — Robotics & Artificial Intelligence

---

## 1. Project Purpose & Scope

The **Smart Campus Management System** manages academic workflows within an emerging-technology academic department. It serves as a dual-purpose system:
1. **Academic Management Platform**: Automates student records, faculty allocations, course registrations, attendance tracking, examination marks, scheduling, digital notices, and verifiable document requests.
2. **DBMS Rigorous Reference**: Built on a normalized (3NF) relational database schema in MySQL, backed by Spring Boot and JPA/Hibernate with explicit foreign key integrity, domain constraints, audit trails, and multi-table analytical queries.

### Core User Roles

| Role | Responsibilities |
|---|---|
| **ADMIN** | Departmental configuration, user provisioning, classroom scheduling, notice dissemination, document approval and issuance. |
| **FACULTY** | Managing course assignments, conducting daily attendance, configuring exams, recording marks/grades. |
| **STUDENT** | Tracking enrolled courses, attendance percentages, exam results, viewing notices, requesting digital certificates (e.g., bonafide, transcripts). |

---

## 2. Technology Stack

- **Platform & Language**: Java 21 (LTS)
- **Framework**: Spring Boot 4.1.1
- **Persistence & ORM**: Spring Data JPA, Hibernate 7.x
- **Security Foundation**: Spring Security 7.x (BCrypt password hashing, stateless architecture)
- **Database**: MySQL 8.x
- **In-Memory Testing**: H2 Database
- **Build Tool**: Apache Maven (with Maven Wrapper `./mvnw`)
- **Validation**: Jakarta Bean Validation (Hibernate Validator)
- **Utilities**: Project Lombok

---

## 3. Database Architecture & Schema Overview

The database design is documented in detail in [`docs/database-design.md`](docs/database-design.md). It consists of **16 normalized tables** with strict referential integrity (`ON UPDATE CASCADE`, `ON DELETE RESTRICT/SET NULL/CASCADE`):

```text
DEPARTMENT
   ├──────────< PROGRAM ──────────< STUDENT
   │                  └──────────< COURSE
   └──────────< FACULTY

STUDENT >──── ENROLLMENT ────< COURSE
STUDENT + COURSE ────────────< ATTENDANCE
COURSE ───────────< EXAM ────< MARK >──── STUDENT
COURSE + PROGRAM + FACULTY + CLASSROOM ───< TIMETABLE
USER ──────────────< NOTICE
STUDENT ───────────< DOCUMENT_REQUEST >──── DOCUMENT_TYPE
DOCUMENT_REQUEST ──< DOCUMENT_REQUEST_HISTORY
```

### Relational Tables (16 Entities)

1. `users` — Authentication credentials (BCrypt hashed), system role, active status.
2. `departments` — Academic department identity.
3. `programs` — Degree programs (AIML, IOT, RAI) under a department.
4. `students` — Student profiles linked 1-to-1 to users and many-to-1 to programs.
5. `faculty` — Faculty profiles linked 1-to-1 to users and many-to-1 to departments.
6. `courses` — Curriculum offerings per program, credit definitions, faculty assignments.
7. `enrollments` — Student-course junction table with composite unique term constraints.
8. `attendance` — Daily attendance logs per session with status (`PRESENT`, `ABSENT`, `LATE`).
9. `exams` — Examinations (`MID_1`, `MID_2`, `LAB`, `END_SEMESTER`) linked to courses.
10. `marks` — Student assessment scores with unique `(exam_id, student_id)` constraint.
11. `classrooms` — Physical campus lecture halls and laboratory venues with capacity.
12. `timetable` — Scheduled class slots preventing room, faculty, and program conflicts.
13. `notices` — Department bulletins with priority, categories, and target program scopes.
14. `document_types` — Supported certificate configurations (Bonafide, Transcripts, NOC).
15. `document_requests` — Student certificate issuance lifecycle (`SUBMITTED` → `ISSUED`).
16. `document_request_history` — Audit trail tracking status transitions, timestamps, and reviewers.

---

## 4. Project Structure

```text
Smart-Campus-Management-System/
├── docs/
│   └── database-design.md         # Full database design specification
├── sql/
│   ├── schema.sql                 # Standalone MySQL DDL for all 16 tables
│   ├── seed-data.sql              # Realistic academic demonstration dataset
│   └── queries.sql                # 13 academic SQL queries (JOINs, GROUP BY, aggregations)
├── backend/
│   ├── pom.xml                    # Maven dependencies and build configuration
│   ├── mvnw / mvnw.cmd            # Maven wrapper executables
│   └── src/
│       ├── main/
│       │   ├── java/com/smartcampus/
│       │   │   ├── SmartCampusApplication.java
│       │   │   ├── config/        # SecurityConfig, CorsConfig
│       │   │   ├── entity/        # 16 JPA Entities & 9 Domain Enums
│       │   │   ├── repository/    # 16 Spring Data JPA Repositories
│       │   │   ├── dto/response/  # ApiResponse standard envelope
│       │   │   └── exception/     # GlobalExceptionHandler, Custom exceptions
│       │   └── resources/
│       │       └── application.properties # Main MySQL configuration (ddl-auto=validate)
│       └── test/
│           ├── java/com/smartcampus/
│           │   └── SmartCampusApplicationTests.java
│           └── resources/
│               └── application.properties # H2 in-memory test configuration
├── frontend/                      # Placeholder for future UI development
├── .env.example                   # Environment variable template
├── .gitignore
└── README.md
```

---

## 5. Local Setup & Installation

### Prerequisites

- **Java Development Kit (JDK)**: Version 21 or higher
- **MySQL Server**: Version 8.0 or higher
- **Maven**: 3.9+ (or use included `./mvnw`)

### Step 1: Clone Repository & Configure Environment

```bash
git clone https://github.com/palleti-vamshi/Smart-Campus-Management-System.git
cd Smart-Campus-Management-System

# Copy sample environment configuration
cp .env.example .env
```

Edit `.env` or set system environment variables:
```dotenv
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=your-production-secret-key-must-be-at-least-256-bits-long
```

### Step 2: Database Initialization (MySQL)

Create the database and import the schema and demonstration data:

```bash
# 1. Create tables, constraints, and secondary indexes
mysql -u root -p < sql/schema.sql

# 2. Populate realistic seed data across all 16 tables
mysql -u root -p < sql/seed-data.sql

# 3. (Optional) Run demonstration DBMS queries
mysql -u root -p < sql/queries.sql
```

> **Default Seed Accounts**: All generated demo users have password: `Password@123`
> - Admin: `admin` (`admin@smartcampus.edu`)
> - Faculty: `prof.sharma`, `dr.reddy`, `prof.iyer`, `dr.patel`
> - Students: `john.doe`, `jane.smith`, `rahul.verma`, `priya.nair`, `arjun.kumar`, `sneha.rao`

### Step 3: Build & Test Backend

```bash
cd backend

# Run automated tests (executes against isolated H2 in-memory DB)
./mvnw clean test

# Build production JAR package
./mvnw package
```

### Step 4: Run Application

```bash
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080`.

> **Hibernate Validation**: Main runtime configuration is set to `spring.jpa.hibernate.ddl-auto=validate`. Hibernate verifies entity mappings against the MySQL schema created by `sql/schema.sql` without making uncontrolled modifications.

---

## 6. Implementation Status

| Milestone | Status | Description |
|---|---|---|
| **Phase 1: Database & Foundation** | **COMPLETE** | 16 JPA entities, 9 enums, 16 Spring Data JPA repositories, complete MySQL DDL (`schema.sql`), realistic demo data (`seed-data.sql`), 13 DBMS queries (`queries.sql`), exception handling framework, CORS & security foundation, test suite passing. |
| **Phase 2: Authentication & Authorization** | *PENDING* | JWT token provider, filter chain, UserDetailsService, Login/Register DTOs, AuthController, role-based endpoint authorization. |
| **Phase 3: Academic Business Modules** | *PENDING* | Services, DTOs, and REST Controllers for Student, Faculty, Course, Attendance, Exam, and Notice management. |
| **Phase 4: Document Workflow & Dashboard** | *PENDING* | Digital certificate generator, request approval lifecycle, audit trails, and role-specific dashboard metrics. |
| **Phase 5: Frontend Interface** | *PENDING* | Modern responsive web UI connecting to backend REST APIs. |

> **Note**: In accordance with the Phase 1 milestone, authentication endpoints (JWT login/register), business service layers, and REST controllers have intentionally **not** been implemented yet.
