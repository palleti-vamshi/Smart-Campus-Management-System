# Phase 10: Academic Progress & Department Dashboard API Documentation

## 1. Overview & Purpose
The **Academic Progress & Department Dashboard** module provides unified, read-only analytics and progress metrics for the Smart Campus Management System.

### Core Principles
- **Strictly Read-Only**: The dashboard services execute purely analytical reads (`@Transactional(readOnly = true)`). They never insert, update, or delete records.
- **Zero Schema Redundancy**: No separate tables (such as `dashboard_stats` or `analytics`) are created. All metrics are aggregated directly on-demand from existing domain entities across Phases 1 through 9.
- **Database-Side Aggregation**: Utilizes SQL/JPQL aggregate functions (`COUNT`, `SUM`, `AVG`, `GROUP BY`, conditional projections) to prevent memory-intensive N+1 query loops.
- **Role Isolation**: Strictly enforces that students only access their own academic progress and faculty only access their assigned courses, derived entirely from JWT authentication context.

---

## 2. Role-Specific Dashboards

| Role | Scope | Key Capabilities |
|---|---|---|
| **ADMIN** | Department-Wide | Total students, faculty, programs, courses, classrooms; student distribution by program/semester; department attendance %; exam/mark metrics; enrollments by status/program; timetable summary; active notice statistics; document request queue metrics; upcoming academic exams. |
| **FACULTY** | Assigned Courses Only | Assigned courses list; enrolled students count; course-wise enrollments; attendance percentages for assigned courses; upcoming exams for assigned courses; marks entered & course averages; faculty timetable schedule; department notices. |
| **STUDENT** | Authenticated Student Only | Sanitized student profile; overall & course-wise attendance breakdown; upcoming & past enrolled exams; exam marks & grades; active enrollments; student program timetable; program/department-wide active notices; own document request lifecycle counts; quick overview badges. |

---

## 3. Endpoints & Authorization

| Method | Endpoint | Authorized Roles | Description |
|---|---|---|---|
| `GET` | `/api/admin/dashboard` | `ADMIN` (`ROLE_ADMIN`) | Returns the department-wide administrator dashboard. |
| `GET` | `/api/faculty/dashboard` | `FACULTY` (`ROLE_FACULTY`) | Returns the faculty dashboard scoped to the authenticated instructor's assigned courses. |
| `GET` | `/api/student/dashboard` | `STUDENT` (`ROLE_STUDENT`) | Returns the student dashboard scoped to the authenticated learner. |

Identity is extracted exclusively from the validated JWT token (`@AuthenticationPrincipal CustomUserDetails`). Client-supplied identifiers (`userId`, `studentId`, `facultyId`) in headers or query parameters are ignored.

---

## 4. Attendance Calculations
Attendance metrics follow Phase 5 rules:
$$\text{Attendance Percentage} = \frac{\text{Present Records}}{\text{Total Attendance Records}} \times 100$$
- Where no attendance records exist, the API returns `0.0` (never `NaN`, `Infinity`, or an error).
- Values are formatted to 2 decimal places using `RoundingMode.HALF_UP`.
- **Student Scoping**: Restricted to attendance records where `attendance.student.studentId = :studentId`.
- **Faculty Scoping**: Restricted to attendance records for courses assigned to `attendance.course.faculty.facultyId = :facultyId`.

---

## 5. Academic Metrics & Performance
- Reuses existing `Exam` and `Mark` entities.
- Calculates total exams, marks recorded, and average score across courses.
- Courses with no marks recorded return safe zeroes.
- Grade letters (e.g., `A+`, `A`, `B`) and remarks are passed through directly from existing Phase 6 evaluations without inventing new grading algorithms.

---

## 6. Notice Visibility Rules
Strictly preserves Phase 8 filtering logic:
- Only active notices (`publishAt <= now` and `expiresAt >= now` or `expiresAt IS NULL`) are counted.
- **Admin**: All department active notices.
- **Faculty**: Department-wide notices.
- **Student**: Department-wide notices + program-specific notices matching `student.program.programId`.

---

## 7. Document Request Summary
Aggregates document request states from Phase 9:
- `totalRequests`: Total requests within scope.
- `pendingRequests`: Sum of `SUBMITTED` + `UNDER_REVIEW`.
- `submittedRequests`: Requests in `SUBMITTED` status.
- `underReviewRequests`: Requests in `UNDER_REVIEW` status.
- `approvedRequests`: Requests in `APPROVED` status.
- `issuedRequests`: Requests in `ISSUED` status.
- `rejectedRequests`: Requests in `REJECTED` status.
- **Student Scoping**: Queries restricted to `documentRequest.student.studentId = :studentId`.

---

## 8. Student & Faculty Ownership Rules
- **Profile Sanitization**: Password hashes, salt keys, and internal security tokens are omitted from `StudentProfileSummary`.
- **Enrollment Scoping**: Students only view courses with active enrollment records (`student_id = :studentId`).
- **Timetable Filtering**:
  - Student timetable queries match `program_id = :programId` and `semester = :semester`.
  - Faculty timetable queries match `faculty_id = :facultyId`.

---

## 9. Response Structures & Examples

### 9.1 Admin Dashboard (`GET /api/admin/dashboard`)
```json
{
  "success": true,
  "message": "Admin dashboard retrieved successfully",
  "data": {
    "departmentSummary": {
      "totalStudents": 338,
      "totalFaculty": 51,
      "totalPrograms": 6,
      "totalCourses": 42,
      "totalClassrooms": 18
    },
    "studentsByProgram": [
      {
        "programId": 1,
        "programCode": "CSE-AIML",
        "programName": "B.Tech in Artificial Intelligence & Machine Learning",
        "studentCount": 120
      }
    ],
    "studentsBySemester": [
      {
        "semester": 3,
        "studentCount": 160
      }
    ],
    "attendanceOverview": {
      "totalClasses": 1420,
      "presentClasses": 1220,
      "absentClasses": 150,
      "lateClasses": 50,
      "overallPercentage": 85.92,
      "byProgram": [],
      "byCourse": []
    },
    "academicPerformance": {
      "examCount": 14,
      "marksCount": 420,
      "averageMarks": 74.25,
      "coursePerformance": []
    },
    "enrollmentSummary": {
      "totalEnrollments": 850,
      "activeEnrollments": 810,
      "completedEnrollments": 40,
      "droppedEnrollments": 0,
      "byProgram": []
    },
    "timetableSummary": {
      "totalEntries": 48,
      "todayClassesCount": 12,
      "todaySchedule": [],
      "weeklySchedule": []
    },
    "noticeSummary": {
      "totalActive": 5,
      "urgentCount": 2,
      "byCategory": {
        "EXAM": 2,
        "EVENT": 3
      },
      "recentNotices": []
    },
    "documentSummary": {
      "totalRequests": 34,
      "pendingRequests": 8,
      "submittedRequests": 5,
      "underReviewRequests": 3,
      "approvedRequests": 10,
      "issuedRequests": 14,
      "rejectedRequests": 2
    },
    "upcomingExams": [
      {
        "examId": 101,
        "courseId": 12,
        "courseCode": "CS301",
        "courseName": "Machine Learning",
        "examName": "Mid Term 1",
        "examType": "MID_1",
        "examDate": "2026-10-15",
        "maxMarks": 50.0
      }
    ]
  },
  "timestamp": "2026-09-28T02:45:00"
}
```

### 9.2 Faculty Dashboard (`GET /api/faculty/dashboard`)
```json
{
  "success": true,
  "message": "Faculty dashboard retrieved successfully",
  "data": {
    "facultyId": 5,
    "facultyName": "Dr. Alan Turing",
    "employeeCode": "FAC001",
    "designation": "Professor",
    "departmentName": "Computer Science and Engineering",
    "totalAssignedCourses": 2,
    "assignedCourses": [
      {
        "courseId": 12,
        "courseCode": "CS301",
        "courseName": "Machine Learning",
        "credits": 4.0,
        "semester": 3,
        "programCode": "CSE-AIML",
        "facultyName": "Dr. Alan Turing"
      }
    ],
    "studentEnrollmentSummary": {
      "totalDistinctStudents": 60,
      "totalEnrollments": 60,
      "enrollmentsByCourse": []
    },
    "attendanceOverview": {
      "totalClasses": 90,
      "presentClasses": 82,
      "absentClasses": 5,
      "lateClasses": 3,
      "overallPercentage": 91.11,
      "byProgram": [],
      "byCourse": []
    },
    "examSummary": {
      "totalExams": 2,
      "upcomingExams": [],
      "examsByCourse": []
    },
    "marksSummary": {
      "marksEnteredCount": 60,
      "coursePerformance": []
    },
    "timetableSummary": {
      "totalEntries": 4,
      "todayClassesCount": 1,
      "todaySchedule": [],
      "weeklySchedule": []
    },
    "noticeSummary": {
      "totalActive": 3,
      "urgentCount": 1,
      "byCategory": {},
      "recentNotices": []
    }
  },
  "timestamp": "2026-09-28T02:45:00"
}
```

### 9.3 Student Dashboard (`GET /api/student/dashboard`)
```json
{
  "success": true,
  "message": "Student dashboard retrieved successfully",
  "data": {
    "profile": {
      "studentId": 101,
      "rollNumber": "23AIML001",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe",
      "email": "student1@smartcampus.edu",
      "currentSemester": 3,
      "section": "A",
      "programCode": "CSE-AIML",
      "programName": "B.Tech in Artificial Intelligence & Machine Learning",
      "admissionYear": 2023
    },
    "quickSummary": {
      "overallAttendancePercentage": 92.5,
      "upcomingExamsCount": 2,
      "pendingDocumentRequests": 1,
      "activeNoticesCount": 3,
      "enrolledCoursesCount": 5
    },
    "attendance": {
      "totalClasses": 40,
      "presentClasses": 37,
      "absentClasses": 2,
      "lateClasses": 1,
      "overallPercentage": 92.5,
      "byProgram": [],
      "byCourse": []
    },
    "upcomingExams": [
      {
        "examId": 101,
        "courseId": 12,
        "courseCode": "CS301",
        "courseName": "Machine Learning",
        "examName": "Mid Term 1",
        "examType": "MID_1",
        "examDate": "2026-10-15",
        "maxMarks": 50.0
      }
    ],
    "pastExams": [],
    "marks": {
      "totalMarksRecorded": 3,
      "averageMarks": 84.5,
      "marks": [
        {
          "markId": 501,
          "courseCode": "CS301",
          "courseName": "Machine Learning",
          "examName": "Mid Term 1",
          "examType": "MID_1",
          "marksObtained": 45.0,
          "maxMarks": 50.0,
          "grade": "A+"
        }
      ]
    },
    "enrollments": [],
    "timetable": {
      "totalEntries": 5,
      "todayClassesCount": 2,
      "todaySchedule": [],
      "weeklySchedule": []
    },
    "notices": {
      "totalActive": 3,
      "urgentCount": 1,
      "byCategory": {},
      "recentNotices": []
    },
    "documentSummary": {
      "totalRequests": 2,
      "pendingRequests": 1,
      "submittedRequests": 1,
      "underReviewRequests": 0,
      "approvedRequests": 1,
      "issuedRequests": 0,
      "rejectedRequests": 0
    }
  },
  "timestamp": "2026-09-28T02:45:00"
}
```

---

## 10. Error Responses

| Scenario | HTTP Status | Code | Description |
|---|---|---|---|
| Unauthenticated request | `401 Unauthorized` | 401 | Missing, malformed, or expired JWT bearer token. |
| Role forbidden (e.g. Student accessing Admin) | `403 Forbidden` | 403 | Authenticated user lacks the requisite authority. |
| User profile missing | `404 Not Found` | 404 | Authenticated user has no linked Student or Faculty record. |
| General server error | `500 Internal Server Error` | 500 | Unexpected backend execution exception. |
