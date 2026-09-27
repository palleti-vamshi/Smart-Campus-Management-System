-- ====================================
-- Smart Campus Management System
-- Demonstration DBMS SQL Queries
-- MySQL 8+
-- ====================================
-- This file contains academic and practical DBMS queries demonstrating:
-- 1. INNER JOIN (Multi-table course enrollments)
-- 2. LEFT JOIN (Faculty course allocations including unassigned)
-- 3. AGGREGATE FUNCTIONS & GROUP BY (Attendance percentage calculation)
-- 4. GROUP BY with HAVING (Attendance defaulters below 75%)
-- 5. AGGREGATE & JOIN (Student examination performance and average marks)
-- 6. GROUP BY & COUNT (Course enrollment summary statistics)
-- 7. SUBQUERY (Students scoring above course average)
-- 8. MULTI-TABLE JOIN & FILTER (Pending document requests workflow queue)
-- 9. AUDIT TRAIL / TEMPORAL TRACING (Document request transition history)
-- 10. CONDITIONAL FILTERING & NULL LOGIC (Active departmental and program notices)
-- 11. DATE/TIME QUERIES (Upcoming exams schedule)
-- 12. SCALAR SUBQUERIES (Department-level executive dashboard summary)
-- 13. TIMETABLE SCHEDULE (Daily classroom allocation matrix)
-- ====================================

USE smart_campus;

-- ====================================================================
-- Query 1: Student Enrolled Courses with Program & Credit Details
-- Demonstrates: INNER JOIN across 4 tables (students, programs, enrollments, courses)
-- Purpose: Generates the official course registration slip for a student.
-- ====================================================================
SELECT 
    s.roll_number,
    CONCAT(s.first_name, ' ', COALESCE(s.last_name, '')) AS student_name,
    p.program_code,
    c.course_code,
    c.course_name,
    c.credits,
    c.course_type,
    e.academic_year,
    e.semester,
    e.status AS enrollment_status
FROM students s
INNER JOIN programs p ON s.program_id = p.program_id
INNER JOIN enrollments e ON s.student_id = e.student_id
INNER JOIN courses c ON e.course_id = c.course_id
WHERE s.roll_number = '25071A6601'
ORDER BY c.course_code;

-- ====================================================================
-- Query 2: Faculty Workload and Course Teaching Assignments
-- Demonstrates: LEFT JOIN to include faculty members who have zero assigned courses
-- Purpose: Helps department head audit teaching load distribution.
-- ====================================================================
SELECT 
    f.employee_code,
    CONCAT(f.first_name, ' ', COALESCE(f.last_name, '')) AS faculty_name,
    f.designation,
    f.specialization,
    COALESCE(c.course_code, 'UNASSIGNED') AS course_code,
    COALESCE(c.course_name, 'No Course Assigned') AS course_name,
    COALESCE(c.credits, 0.0) AS course_credits,
    p.program_code
FROM faculty f
LEFT JOIN courses c ON f.faculty_id = c.faculty_id
LEFT JOIN programs p ON c.program_id = p.program_id
ORDER BY f.employee_code, c.course_code;

-- ====================================================================
-- Query 3: Attendance Percentage per Student per Course
-- Demonstrates: SUM(CASE ...), COUNT(*), and ROUND() for percentage calculation
-- Purpose: Evaluates individual student attendance records against institutional eligibility.
-- ====================================================================
SELECT 
    s.roll_number,
    CONCAT(s.first_name, ' ', COALESCE(s.last_name, '')) AS student_name,
    c.course_code,
    c.course_name,
    COUNT(a.attendance_id) AS total_classes_conducted,
    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS attended_classes,
    SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) AS late_classes,
    SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_classes,
    ROUND(
        (SUM(CASE WHEN a.status = 'PRESENT' THEN 1.0 
                  WHEN a.status = 'LATE' THEN 0.5 
                  ELSE 0.0 END) * 100.0) / COUNT(a.attendance_id), 
        2
    ) AS effective_attendance_percentage
FROM attendance a
INNER JOIN students s ON a.student_id = s.student_id
INNER JOIN courses c ON a.course_id = c.course_id
GROUP BY s.student_id, s.roll_number, s.first_name, s.last_name, c.course_id, c.course_code, c.course_name
ORDER BY c.course_code, s.roll_number;

-- ====================================================================
-- Query 4: Attendance Defaulter List (Below 75% Threshold)
-- Demonstrates: GROUP BY with HAVING clause for aggregated threshold filtering
-- Purpose: Flags students barred from sitting for end-semester exams due to poor attendance.
-- ====================================================================
SELECT 
    s.roll_number,
    CONCAT(s.first_name, ' ', COALESCE(s.last_name, '')) AS student_name,
    c.course_code,
    c.course_name,
    COUNT(a.attendance_id) AS total_sessions,
    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS sessions_attended,
    ROUND((SUM(CASE WHEN a.status = 'PRESENT' THEN 1.0 ELSE 0.0 END) * 100.0) / COUNT(a.attendance_id), 2) AS attendance_pct
FROM attendance a
INNER JOIN students s ON a.student_id = s.student_id
INNER JOIN courses c ON a.course_id = c.course_id
GROUP BY s.student_id, s.roll_number, s.first_name, s.last_name, c.course_id, c.course_code, c.course_name
HAVING attendance_pct < 75.00
ORDER BY attendance_pct ASC;

-- ====================================================================
-- Query 5: Student Academic Performance & Marks Summary
-- Demonstrates: Multi-table JOIN with AVG(), MAX(), MIN() aggregate functions
-- Purpose: Generates cumulative internal assessment report for each student.
-- ====================================================================
SELECT 
    s.roll_number,
    CONCAT(s.first_name, ' ', COALESCE(s.last_name, '')) AS student_name,
    p.program_code,
    COUNT(m.mark_id) AS exams_taken,
    ROUND(AVG((m.marks_obtained / ex.max_marks) * 100.0), 2) AS average_percentage,
    ROUND(MAX((m.marks_obtained / ex.max_marks) * 100.0), 2) AS highest_percentage,
    ROUND(MIN((m.marks_obtained / ex.max_marks) * 100.0), 2) AS lowest_percentage
FROM marks m
INNER JOIN students s ON m.student_id = s.student_id
INNER JOIN programs p ON s.program_id = p.program_id
INNER JOIN exams ex ON m.exam_id = ex.exam_id
GROUP BY s.student_id, s.roll_number, s.first_name, s.last_name, p.program_code
ORDER BY average_percentage DESC;

-- ====================================================================
-- Query 6: Course-wise Enrollment Statistics
-- Demonstrates: COUNT(e.student_id) with GROUP BY and LEFT JOIN
-- Purpose: Class size and department resource allocation planning.
-- ====================================================================
SELECT 
    c.course_code,
    c.course_name,
    c.course_type,
    p.program_code,
    COUNT(e.enrollment_id) AS total_enrolled_students,
    SUM(CASE WHEN e.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_students
FROM courses c
INNER JOIN programs p ON c.program_id = p.program_id
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_code, c.course_name, c.course_type, p.program_code
ORDER BY total_enrolled_students DESC, c.course_code;

-- ====================================================================
-- Query 7: Students Scoring Above the Course Average (Subquery)
-- Demonstrates: Correlated / Non-correlated SUBQUERY in WHERE clause
-- Purpose: Identifies distinction/merit students for honors programs.
-- ====================================================================
SELECT 
    s.roll_number,
    CONCAT(s.first_name, ' ', COALESCE(s.last_name, '')) AS student_name,
    c.course_code,
    ex.exam_name,
    m.marks_obtained,
    ex.max_marks,
    m.grade
FROM marks m
INNER JOIN students s ON m.student_id = s.student_id
INNER JOIN exams ex ON m.exam_id = ex.exam_id
INNER JOIN courses c ON ex.course_id = c.course_id
WHERE m.marks_obtained > (
    SELECT AVG(inner_m.marks_obtained)
    FROM marks inner_m
    WHERE inner_m.exam_id = m.exam_id
)
ORDER BY c.course_code, m.marks_obtained DESC;

-- ====================================================================
-- Query 8: Pending Document Requests Workflow Queue
-- Demonstrates: Multi-table JOIN with WHERE IN filtering and ORDER BY
-- Purpose: Powers the Administrative / Academic Office action inbox.
-- ====================================================================
SELECT 
    dr.request_id,
    dr.request_number,
    s.roll_number,
    CONCAT(s.first_name, ' ', COALESCE(s.last_name, '')) AS student_name,
    p.program_code,
    dt.document_name,
    dr.purpose,
    dr.status,
    dr.submitted_at,
    TIMESTAMPDIFF(HOUR, dr.submitted_at, NOW()) AS hours_pending
FROM document_requests dr
INNER JOIN students s ON dr.student_id = s.student_id
INNER JOIN programs p ON s.program_id = p.program_id
INNER JOIN document_types dt ON dr.document_type_id = dt.document_type_id
WHERE dr.status IN ('SUBMITTED', 'UNDER_REVIEW')
ORDER BY dr.submitted_at ASC;

-- ====================================================================
-- Query 9: Document Request Audit Trail & State Transitions
-- Demonstrates: Chronological tracking via history table joined with users
-- Purpose: Complete accountability audit log for compliance and verification.
-- ====================================================================
SELECT 
    dr.request_number,
    dt.document_name,
    drh.old_status,
    drh.new_status,
    u.username AS transitioned_by,
    u.role AS actor_role,
    drh.remarks,
    drh.changed_at
FROM document_request_history drh
INNER JOIN document_requests dr ON drh.request_id = dr.request_id
INNER JOIN document_types dt ON dr.document_type_id = dt.document_type_id
INNER JOIN users u ON drh.changed_by = u.user_id
WHERE dr.request_number = 'REQ-2024-0001'
ORDER BY drh.changed_at ASC;

-- ====================================================================
-- Query 10: Active Departmental and Program-Targeted Notices
-- Demonstrates: Date range checking and NULL handling (department-wide vs program)
-- Purpose: Feeds the student noticeboard notification feed.
-- ====================================================================
SELECT 
    n.notice_id,
    n.title,
    n.category,
    n.priority,
    COALESCE(p.program_code, 'ALL PROGRAMS') AS target_audience,
    u.username AS posted_by,
    n.publish_at,
    n.expires_at
FROM notices n
INNER JOIN users u ON n.published_by = u.user_id
LEFT JOIN programs p ON n.target_program_id = p.program_id
WHERE n.publish_at <= NOW()
  AND (n.expires_at IS NULL OR n.expires_at >= NOW())
  AND (n.target_program_id = 1 OR n.target_program_id IS NULL)
ORDER BY 
    CASE n.priority 
        WHEN 'URGENT' THEN 1 
        WHEN 'HIGH' THEN 2 
        WHEN 'NORMAL' THEN 3 
        ELSE 4 
    END,
    n.publish_at DESC;

-- ====================================================================
-- Query 11: Upcoming Examination Schedule
-- Demonstrates: Date comparisons and multi-table lookup for scheduling
-- Purpose: Student exam hall ticket and calendar display.
-- ====================================================================
SELECT 
    ex.exam_id,
    p.program_code,
    c.course_code,
    c.course_name,
    ex.exam_name,
    ex.exam_type,
    ex.exam_date,
    ex.max_marks,
    DATEDIFF(ex.exam_date, CURRENT_DATE) AS days_until_exam
FROM exams ex
INNER JOIN courses c ON ex.course_id = c.course_id
INNER JOIN programs p ON c.program_id = p.program_id
WHERE ex.exam_date >= CURRENT_DATE
ORDER BY ex.exam_date ASC;

-- ====================================================================
-- Query 12: Department Executive Dashboard Summary
-- Demonstrates: SCALAR SUBQUERIES aggregated into a single reporting row
-- Purpose: Provides high-level KPIs for Department Head / Admin portal.
-- ====================================================================
SELECT 
    (SELECT COUNT(*) FROM students) AS total_active_students,
    (SELECT COUNT(*) FROM faculty) AS total_faculty_members,
    (SELECT COUNT(*) FROM programs) AS total_programs,
    (SELECT COUNT(*) FROM courses) AS total_courses_offered,
    (SELECT COUNT(*) FROM document_requests WHERE status IN ('SUBMITTED', 'UNDER_REVIEW')) AS pending_document_requests,
    (SELECT COUNT(*) FROM notices WHERE publish_at <= NOW() AND (expires_at IS NULL OR expires_at >= NOW())) AS active_bulletin_notices;

-- ====================================================================
-- Query 13: Daily Classroom Allocation & Timetable Schedule
-- Demonstrates: Complex multi-table JOIN mapping timetable constraints
-- Purpose: Avoids room clashes and outputs physical room schedules.
-- ====================================================================
SELECT 
    t.day_of_week,
    t.start_time,
    t.end_time,
    cr.room_number,
    cr.building,
    cr.room_type,
    p.program_code,
    c.course_code,
    c.course_name,
    CONCAT(f.first_name, ' ', COALESCE(f.last_name, '')) AS instructor_name
FROM timetable t
INNER JOIN classrooms cr ON t.classroom_id = cr.classroom_id
INNER JOIN programs p ON t.program_id = p.program_id
INNER JOIN courses c ON t.course_id = c.course_id
INNER JOIN faculty f ON t.faculty_id = f.faculty_id
WHERE t.day_of_week = 'MONDAY'
ORDER BY t.start_time ASC, cr.room_number ASC;
