-- ====================================
-- Smart Campus Management System
-- Complete Database Schema (DDL)
-- MySQL 8+
-- ====================================
--
-- Source of truth: docs/database-design.md
-- JPA entities: backend/src/main/java/com/smartcampus/entity/
--
-- This standalone DDL script can be run independently of Spring Boot
-- for DBMS academic evaluation.
--
-- Execution order respects foreign key dependencies.
-- ====================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS smart_campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_campus;

-- ====================================
-- 1. users
-- ====================================
CREATE TABLE IF NOT EXISTS users (
    user_id         BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_username    UNIQUE (username),
    CONSTRAINT uk_users_email       UNIQUE (email),
    CONSTRAINT chk_users_role       CHECK (role IN ('ADMIN', 'FACULTY', 'STUDENT'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role  ON users (role);

-- ====================================
-- 2. departments
-- ====================================
CREATE TABLE IF NOT EXISTS departments (
    department_id   BIGINT          AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(20)     NOT NULL,
    department_name VARCHAR(150)    NOT NULL,
    description     TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_departments_code  UNIQUE (department_code)
);

-- ====================================
-- 3. programs
-- ====================================
CREATE TABLE IF NOT EXISTS programs (
    program_id      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    department_id   BIGINT          NOT NULL,
    program_code    VARCHAR(20)     NOT NULL,
    program_name    VARCHAR(150)    NOT NULL,
    duration_years  INT             NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_programs_code     UNIQUE (program_code),
    CONSTRAINT fk_programs_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_programs_department ON programs (department_id);

-- ====================================
-- 4. students
-- ====================================
CREATE TABLE IF NOT EXISTS students (
    student_id      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    program_id      BIGINT          NOT NULL,
    roll_number     VARCHAR(50)     NOT NULL,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NULL,
    date_of_birth   DATE            NULL,
    gender          VARCHAR(20)     NULL,
    admission_year  INT             NOT NULL,
    current_semester INT            NOT NULL,
    section         VARCHAR(10)     NOT NULL DEFAULT 'A',
    phone           VARCHAR(20)     NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_students_user         UNIQUE (user_id),
    CONSTRAINT uk_students_roll_number  UNIQUE (roll_number),
    CONSTRAINT fk_students_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_students_program
        FOREIGN KEY (program_id) REFERENCES programs (program_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_students_program ON students (program_id);
CREATE INDEX idx_students_section ON students (section);

-- ====================================
-- 5. faculty
-- ====================================
CREATE TABLE IF NOT EXISTS faculty (
    faculty_id      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    department_id   BIGINT          NOT NULL,
    employee_code   VARCHAR(50)     NOT NULL,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NULL,
    designation     VARCHAR(100)    NULL,
    specialization  VARCHAR(150)    NULL,
    phone           VARCHAR(20)     NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_faculty_user          UNIQUE (user_id),
    CONSTRAINT uk_faculty_employee_code UNIQUE (employee_code),
    CONSTRAINT fk_faculty_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_faculty_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_faculty_department ON faculty (department_id);

-- ====================================
-- 6. courses
-- ====================================
CREATE TABLE IF NOT EXISTS courses (
    course_id       BIGINT          AUTO_INCREMENT PRIMARY KEY,
    program_id      BIGINT          NOT NULL,
    faculty_id      BIGINT          NULL,
    course_code     VARCHAR(30)     NOT NULL,
    course_name     VARCHAR(150)    NOT NULL,
    credits         DECIMAL(3,1)    NOT NULL,
    semester        INT             NOT NULL,
    course_type     VARCHAR(30)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_courses_code      UNIQUE (course_code),
    CONSTRAINT chk_courses_credits  CHECK (credits >= 0),
    CONSTRAINT chk_courses_type     CHECK (course_type IN ('THEORY', 'LAB', 'PROJECT', 'ELECTIVE')),
    CONSTRAINT fk_courses_program
        FOREIGN KEY (program_id) REFERENCES programs (program_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_courses_faculty
        FOREIGN KEY (faculty_id) REFERENCES faculty (faculty_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_courses_program ON courses (program_id);
CREATE INDEX idx_courses_faculty ON courses (faculty_id);

-- ====================================
-- 7. enrollments
-- ====================================
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id   BIGINT          AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT          NOT NULL,
    course_id       BIGINT          NOT NULL,
    academic_year   VARCHAR(20)     NOT NULL,
    semester        INT             NOT NULL,
    enrollment_date DATE            NOT NULL,
    status          VARCHAR(30)     NOT NULL,

    CONSTRAINT uk_enrollment_student_course_term
        UNIQUE (student_id, course_id, academic_year, semester),
    CONSTRAINT chk_enrollment_status CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED')),
    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id) REFERENCES courses (course_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_enrollments_student ON enrollments (student_id);
CREATE INDEX idx_enrollments_course  ON enrollments (course_id);

-- ====================================
-- 8. attendance
-- ====================================
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id   BIGINT          AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT          NOT NULL,
    course_id       BIGINT          NOT NULL,
    attendance_date DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    marked_by       BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_attendance_student_course_date
        UNIQUE (student_id, course_id, attendance_date),
    CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
    CONSTRAINT fk_attendance_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_attendance_course
        FOREIGN KEY (course_id) REFERENCES courses (course_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_attendance_faculty
        FOREIGN KEY (marked_by) REFERENCES faculty (faculty_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_attendance_student ON attendance (student_id);
CREATE INDEX idx_attendance_course  ON attendance (course_id);
CREATE INDEX idx_attendance_date    ON attendance (attendance_date);

-- ====================================
-- 9. exams
-- ====================================
CREATE TABLE IF NOT EXISTS exams (
    exam_id         BIGINT          AUTO_INCREMENT PRIMARY KEY,
    course_id       BIGINT          NOT NULL,
    exam_name       VARCHAR(100)    NOT NULL,
    exam_type       VARCHAR(50)     NOT NULL,
    exam_date       DATE            NOT NULL,
    max_marks       DECIMAL(5,2)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_exams_max_marks  CHECK (max_marks > 0),
    CONSTRAINT chk_exams_type       CHECK (exam_type IN ('MID_1', 'MID_2', 'END_SEMESTER', 'LAB', 'INTERNAL')),
    CONSTRAINT fk_exams_course
        FOREIGN KEY (course_id) REFERENCES courses (course_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_exams_course ON exams (course_id);

-- ====================================
-- 10. marks
-- ====================================
CREATE TABLE IF NOT EXISTS marks (
    mark_id         BIGINT          AUTO_INCREMENT PRIMARY KEY,
    exam_id         BIGINT          NOT NULL,
    student_id      BIGINT          NOT NULL,
    marks_obtained  DECIMAL(5,2)    NOT NULL,
    grade           VARCHAR(5)      NULL,
    entered_by      BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_marks_exam_student UNIQUE (exam_id, student_id),
    CONSTRAINT chk_marks_obtained   CHECK (marks_obtained >= 0),
    CONSTRAINT fk_marks_exam
        FOREIGN KEY (exam_id) REFERENCES exams (exam_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_marks_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_marks_faculty
        FOREIGN KEY (entered_by) REFERENCES faculty (faculty_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_marks_student ON marks (student_id);
CREATE INDEX idx_marks_exam    ON marks (exam_id);

-- ====================================
-- 11. classrooms
-- ====================================
CREATE TABLE IF NOT EXISTS classrooms (
    classroom_id    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    room_number     VARCHAR(30)     NOT NULL,
    building        VARCHAR(100)    NOT NULL,
    room_type       VARCHAR(50)     NOT NULL,
    capacity        INT             NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_classrooms_room_number UNIQUE (room_number),
    CONSTRAINT chk_classrooms_capacity   CHECK (capacity > 0),
    CONSTRAINT chk_classrooms_type       CHECK (room_type IN ('CLASSROOM', 'LAB', 'SEMINAR_HALL'))
);

-- ====================================
-- 12. timetable
-- ====================================
CREATE TABLE IF NOT EXISTS timetable (
    timetable_id    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    program_id      BIGINT          NOT NULL,
    section         VARCHAR(10)     NOT NULL DEFAULT 'A',
    course_id       BIGINT          NOT NULL,
    faculty_id      BIGINT          NOT NULL,
    classroom_id    BIGINT          NOT NULL,
    day_of_week     VARCHAR(15)     NOT NULL,
    start_time      TIME            NOT NULL,
    end_time        TIME            NOT NULL,
    semester        INT             NOT NULL,
    academic_year   VARCHAR(20)     NOT NULL,

    CONSTRAINT chk_timetable_time   CHECK (start_time < end_time),
    CONSTRAINT fk_timetable_program
        FOREIGN KEY (program_id) REFERENCES programs (program_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_timetable_course
        FOREIGN KEY (course_id) REFERENCES courses (course_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_timetable_faculty
        FOREIGN KEY (faculty_id) REFERENCES faculty (faculty_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_timetable_classroom
        FOREIGN KEY (classroom_id) REFERENCES classrooms (classroom_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_timetable_program   ON timetable (program_id);
CREATE INDEX idx_timetable_section   ON timetable (section);
CREATE INDEX idx_timetable_faculty   ON timetable (faculty_id);
CREATE INDEX idx_timetable_classroom ON timetable (classroom_id);
CREATE INDEX idx_timetable_day_time  ON timetable (day_of_week, start_time);

-- ====================================
-- 13. notices
-- ====================================
CREATE TABLE IF NOT EXISTS notices (
    notice_id           BIGINT          AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(200)    NOT NULL,
    content             TEXT            NOT NULL,
    category            VARCHAR(50)     NOT NULL,
    priority            VARCHAR(20)     NOT NULL,
    target_program_id   BIGINT          NULL,
    published_by        BIGINT          NOT NULL,
    publish_at          TIMESTAMP       NOT NULL,
    expires_at          TIMESTAMP       NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_notices_category CHECK (category IN ('ACADEMIC', 'EXAM', 'EVENT', 'INTERNSHIP', 'PLACEMENT', 'IMPORTANT', 'GENERAL')),
    CONSTRAINT chk_notices_priority CHECK (priority IN ('NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT fk_notices_program
        FOREIGN KEY (target_program_id) REFERENCES programs (program_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_notices_published_by
        FOREIGN KEY (published_by) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_notices_target_program ON notices (target_program_id);
CREATE INDEX idx_notices_publish_at     ON notices (publish_at);
CREATE INDEX idx_notices_published_by   ON notices (published_by);

-- ====================================
-- 14. document_types
-- ====================================
CREATE TABLE IF NOT EXISTS document_types (
    document_type_id    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    document_name       VARCHAR(100)    NOT NULL,
    description         TEXT            NULL,
    requires_approval   BOOLEAN         NOT NULL DEFAULT TRUE,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_document_types_name UNIQUE (document_name)
);

-- ====================================
-- 15. document_requests
-- ====================================
CREATE TABLE IF NOT EXISTS document_requests (
    request_id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    request_number      VARCHAR(50)     NOT NULL,
    student_id          BIGINT          NOT NULL,
    document_type_id    BIGINT          NOT NULL,
    purpose             VARCHAR(255)    NOT NULL,
    additional_details  TEXT            NULL,
    status              VARCHAR(30)     NOT NULL,
    reviewed_by         BIGINT          NULL,
    submitted_at        TIMESTAMP       NOT NULL,
    reviewed_at         TIMESTAMP       NULL,
    issued_at           TIMESTAMP       NULL,
    document_path       VARCHAR(500)    NULL,
    verification_code   VARCHAR(100)    NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_docreq_request_number     UNIQUE (request_number),
    CONSTRAINT uk_docreq_verification_code  UNIQUE (verification_code),
    CONSTRAINT chk_docreq_status CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'ISSUED')),
    CONSTRAINT fk_docreq_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_docreq_document_type
        FOREIGN KEY (document_type_id) REFERENCES document_types (document_type_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_docreq_reviewed_by
        FOREIGN KEY (reviewed_by) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_docreq_student ON document_requests (student_id);
CREATE INDEX idx_docreq_status  ON document_requests (status);

-- ====================================
-- 16. document_request_history
-- ====================================
CREATE TABLE IF NOT EXISTS document_request_history (
    history_id      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    request_id      BIGINT          NOT NULL,
    old_status      VARCHAR(30)     NULL,
    new_status      VARCHAR(30)     NOT NULL,
    changed_by      BIGINT          NOT NULL,
    remarks         TEXT            NULL,
    changed_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_docreqhist_request
        FOREIGN KEY (request_id) REFERENCES document_requests (request_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_docreqhist_changed_by
        FOREIGN KEY (changed_by) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX idx_docreqhist_request ON document_request_history (request_id);
