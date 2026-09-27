package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.*;
import com.smartcampus.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Academic Progress & Department Dashboard Integration Tests (Phase 10)")
class DashboardIntegrationTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private FacultyRepository facultyRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private NoticeRepository noticeRepository;
    @Autowired private DocumentTypeRepository documentTypeRepository;
    @Autowired private DocumentRequestRepository documentRequestRepository;
    @Autowired private DocumentRequestHistoryRepository documentRequestHistoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String adminToken;
    private String faculty1Token;
    private String faculty2Token;
    private String student1Token;
    private String student2Token;

    private User adminUser;
    private User f1User;
    private User f2User;
    private User s1User;
    private User s2User;

    private Department dept;
    private Program program1;
    private Program program2;

    private Faculty faculty1;
    private Faculty faculty2;

    private Student student1;
    private Student student2;

    private Course course1;
    private Course course2;

    private Classroom classroom1;

    private Exam exam1;
    private Exam exam2;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean tables in reverse dependency order
        documentRequestHistoryRepository.deleteAll();
        documentRequestRepository.deleteAll();
        documentTypeRepository.deleteAll();
        noticeRepository.deleteAll();
        timetableRepository.deleteAll();
        classroomRepository.deleteAll();
        markRepository.deleteAll();
        examRepository.deleteAll();
        attendanceRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
        programRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Users
        adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        f1User = userRepository.save(User.builder()
                .username("faculty1")
                .email("faculty1@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        f2User = userRepository.save(User.builder()
                .username("faculty2")
                .email("faculty2@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        s1User = userRepository.save(User.builder()
                .username("student1")
                .email("student1@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        s2User = userRepository.save(User.builder()
                .username("student2")
                .email("student2@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        // 2. Department & Programs
        dept = departmentRepository.save(Department.builder()
                .departmentCode("CSE")
                .departmentName("Computer Science and Engineering")
                .build());

        program1 = programRepository.save(Program.builder()
                .department(dept)
                .programCode("AIML")
                .programName("B.Tech AIML")
                .durationYears(4)
                .build());

        program2 = programRepository.save(Program.builder()
                .department(dept)
                .programCode("DS")
                .programName("B.Tech Data Science")
                .durationYears(4)
                .build());

        // 3. Faculty
        faculty1 = facultyRepository.save(Faculty.builder()
                .user(f1User)
                .department(dept)
                .employeeCode("FAC001")
                .firstName("Alan")
                .lastName("Turing")
                .designation("Professor")
                .build());

        faculty2 = facultyRepository.save(Faculty.builder()
                .user(f2User)
                .department(dept)
                .employeeCode("FAC002")
                .firstName("Ada")
                .lastName("Lovelace")
                .designation("Associate Professor")
                .build());

        // 4. Students
        student1 = studentRepository.save(Student.builder()
                .user(s1User)
                .program(program1)
                .rollNumber("23AIML001")
                .firstName("John")
                .lastName("Doe")
                .admissionYear(2023)
                .currentSemester(3)
                .section("A")
                .build());

        student2 = studentRepository.save(Student.builder()
                .user(s2User)
                .program(program2)
                .rollNumber("23DS001")
                .firstName("Jane")
                .lastName("Smith")
                .admissionYear(2023)
                .currentSemester(3)
                .section("B")
                .build());

        // 5. Courses
        course1 = courseRepository.save(Course.builder()
                .courseCode("CS301")
                .courseName("Machine Learning")
                .credits(new BigDecimal("4.0"))
                .semester(3)
                .program(program1)
                .faculty(faculty1)
                .courseType(CourseType.THEORY)
                .build());

        course2 = courseRepository.save(Course.builder()
                .courseCode("CS302")
                .courseName("Cloud Computing")
                .credits(new BigDecimal("3.0"))
                .semester(3)
                .program(program2)
                .faculty(faculty2)
                .courseType(CourseType.THEORY)
                .build());

        // 6. Enrollments (Student1 -> Course1, Student2 -> Course2)
        enrollmentRepository.save(Enrollment.builder()
                .student(student1)
                .course(course1)
                .academicYear("2025-2026")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .enrollmentDate(LocalDate.now().minusMonths(1))
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .student(student2)
                .course(course2)
                .academicYear("2025-2026")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .enrollmentDate(LocalDate.now().minusMonths(1))
                .build());

        // 7. Attendance
        attendanceRepository.save(Attendance.builder()
                .student(student1)
                .course(course1)
                .attendanceDate(LocalDate.now().minusDays(1))
                .status(AttendanceStatus.PRESENT)
                .markedBy(faculty1)
                .build());

        attendanceRepository.save(Attendance.builder()
                .student(student2)
                .course(course2)
                .attendanceDate(LocalDate.now().minusDays(1))
                .status(AttendanceStatus.ABSENT)
                .markedBy(faculty2)
                .build());

        // 8. Classroom & Timetable
        classroom1 = classroomRepository.save(Classroom.builder()
                .roomNumber("LH-101")
                .building("Main Block")
                .roomType(RoomType.CLASSROOM)
                .capacity(60)
                .isActive(true)
                .build());

        String todayDay = LocalDate.now().getDayOfWeek().name();
        timetableRepository.save(Timetable.builder()
                .course(course1)
                .faculty(faculty1)
                .classroom(classroom1)
                .program(program1)
                .semester(3)
                .dayOfWeek(todayDay)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .academicYear("2025-2026")
                .build());

        // 9. Exams
        exam1 = examRepository.save(Exam.builder()
                .course(course1)
                .examName("ML Mid Term")
                .examType(ExamType.MID_1)
                .examDate(LocalDate.now().plusDays(5))
                .maxMarks(new BigDecimal("50.00"))
                .build());

        exam2 = examRepository.save(Exam.builder()
                .course(course2)
                .examName("Cloud Final")
                .examType(ExamType.END_SEMESTER)
                .examDate(LocalDate.now().plusDays(10))
                .maxMarks(new BigDecimal("100.00"))
                .build());

        // 10. Marks
        markRepository.save(Mark.builder()
                .exam(exam1)
                .student(student1)
                .enteredBy(faculty1)
                .marksObtained(new BigDecimal("45.00"))
                .grade("A+")
                .build());

        markRepository.save(Mark.builder()
                .exam(exam2)
                .student(student2)
                .enteredBy(faculty2)
                .marksObtained(new BigDecimal("80.00"))
                .grade("A")
                .build());

        // 11. Notices
        noticeRepository.save(Notice.builder()
                .title("Campus Hackathon Announcement")
                .content("Annual campus hackathon next month.")
                .category(NoticeCategory.EVENT)
                .priority(NoticePriority.URGENT)
                .targetProgram(null) // Dept-wide
                .publishedBy(adminUser)
                .publishAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusDays(5))
                .build());

        noticeRepository.save(Notice.builder()
                .title("AIML Workshop")
                .content("Special workshop for AIML students.")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.NORMAL)
                .targetProgram(program1)
                .publishedBy(f1User)
                .publishAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusDays(5))
                .build());

        // 12. Documents
        DocumentType docType = documentTypeRepository.save(DocumentType.builder()
                .documentName("BONAFIDE_CERTIFICATE")
                .description("Bonafide Certificate")
                .isActive(true)
                .build());

        documentRequestRepository.save(DocumentRequest.builder()
                .student(student1)
                .documentType(docType)
                .requestNumber("DOC-REQ-2026-0001")
                .purpose("Bank Loan")
                .submittedAt(LocalDateTime.now().minusDays(1))
                .status(DocumentStatus.SUBMITTED)
                .build());

        // 13. Obtain JWT Tokens
        adminToken = obtainToken("admin@smartcampus.edu", "Password@123");
        faculty1Token = obtainToken("faculty1@smartcampus.edu", "Password@123");
        faculty2Token = obtainToken("faculty2@smartcampus.edu", "Password@123");
        student1Token = obtainToken("student1@smartcampus.edu", "Password@123");
        student2Token = obtainToken("student2@smartcampus.edu", "Password@123");
    }

    private String obtainToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    @AfterEach
    void tearDown() {
        documentRequestHistoryRepository.deleteAll();
        documentRequestRepository.deleteAll();
        documentTypeRepository.deleteAll();
        noticeRepository.deleteAll();
        timetableRepository.deleteAll();
        classroomRepository.deleteAll();
        markRepository.deleteAll();
        examRepository.deleteAll();
        attendanceRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
        programRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================================
    // ADMIN DASHBOARD TESTS
    // =========================================================================

    @Test
    @DisplayName("1. Admin can access dashboard -> 200")
    void testAdminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("2. Dashboard contains department summary")
    void testAdminDashboardDepartmentSummary() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departmentSummary.totalStudents").value(2))
                .andExpect(jsonPath("$.data.departmentSummary.totalFaculty").value(2))
                .andExpect(jsonPath("$.data.departmentSummary.totalPrograms").value(2))
                .andExpect(jsonPath("$.data.departmentSummary.totalCourses").value(2))
                .andExpect(jsonPath("$.data.departmentSummary.totalClassrooms").value(1));
    }

    @Test
    @DisplayName("3. Dashboard contains student/program summary")
    void testAdminDashboardStudentProgramSummary() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentsByProgram", hasSize(2)))
                .andExpect(jsonPath("$.data.studentsBySemester", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("4. Dashboard contains attendance summary")
    void testAdminDashboardAttendanceSummary() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceOverview.totalClasses").value(2))
                .andExpect(jsonPath("$.data.attendanceOverview.presentClasses").value(1))
                .andExpect(jsonPath("$.data.attendanceOverview.absentClasses").value(1))
                .andExpect(jsonPath("$.data.attendanceOverview.overallPercentage").value(50.0));
    }

    @Test
    @DisplayName("5. Dashboard contains academic summary")
    void testAdminDashboardAcademicSummary() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.academicPerformance.examCount").value(2))
                .andExpect(jsonPath("$.data.academicPerformance.marksCount").value(2))
                .andExpect(jsonPath("$.data.academicPerformance.averageMarks").value(62.5));
    }

    @Test
    @DisplayName("6. Dashboard contains notice summary")
    void testAdminDashboardNoticeSummary() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeSummary.totalActive").value(2))
                .andExpect(jsonPath("$.data.noticeSummary.urgentCount").value(1));
    }

    @Test
    @DisplayName("7. Dashboard contains document summary")
    void testAdminDashboardDocumentSummary() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentSummary.totalRequests").value(1))
                .andExpect(jsonPath("$.data.documentSummary.pendingRequests").value(1));
    }

    // =========================================================================
    // FACULTY DASHBOARD TESTS
    // =========================================================================

    @Test
    @DisplayName("8. Faculty can access dashboard -> 200")
    void testFacultyCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.facultyName").value("Alan Turing"));
    }

    @Test
    @DisplayName("9. Faculty dashboard only contains assigned courses")
    void testFacultyDashboardOnlyAssignedCourses() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAssignedCourses").value(1))
                .andExpect(jsonPath("$.data.assignedCourses[0].courseCode").value("CS301"))
                .andExpect(jsonPath("$.data.assignedCourses[?(@.courseCode == 'CS302')]").doesNotExist());
    }

    @Test
    @DisplayName("10. Faculty attendance restricted to assigned courses")
    void testFacultyAttendanceRestricted() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceOverview.totalClasses").value(1))
                .andExpect(jsonPath("$.data.attendanceOverview.presentClasses").value(1))
                .andExpect(jsonPath("$.data.attendanceOverview.overallPercentage").value(100.0));
    }

    @Test
    @DisplayName("11. Faculty exams restricted to assigned courses")
    void testFacultyExamsRestricted() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.examSummary.totalExams").value(1))
                .andExpect(jsonPath("$.data.examSummary.upcomingExams[0].examName").value("ML Mid Term"));
    }

    @Test
    @DisplayName("12. Faculty marks restricted to assigned courses")
    void testFacultyMarksRestricted() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marksSummary.marksEnteredCount").value(1))
                .andExpect(jsonPath("$.data.marksSummary.coursePerformance[0].averageMarks").value(45.0));
    }

    @Test
    @DisplayName("13. Faculty timetable restricted to authenticated faculty")
    void testFacultyTimetableRestricted() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timetableSummary.totalEntries").value(1))
                .andExpect(jsonPath("$.data.timetableSummary.todaySchedule[0].courseCode").value("CS301"));

        // Faculty2 should have 0 timetable entries
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + faculty2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timetableSummary.totalEntries").value(0));
    }

    // =========================================================================
    // STUDENT DASHBOARD TESTS
    // =========================================================================

    @Test
    @DisplayName("14. Student can access dashboard -> 200")
    void testStudentCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("15. Student profile summary is correct and excludes sensitive auth data")
    void testStudentProfileSummary() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.rollNumber").value("23AIML001"))
                .andExpect(jsonPath("$.data.profile.firstName").value("John"))
                .andExpect(jsonPath("$.data.profile.programCode").value("AIML"))
                .andExpect(jsonPath("$.data.profile.password").doesNotExist())
                .andExpect(jsonPath("$.data.profile.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("16. Attendance belongs only to authenticated student")
    void testStudentAttendanceBelongsOnlyToStudent() throws Exception {
        // Student1 has 1 present out of 1 -> 100%
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendance.totalClasses").value(1))
                .andExpect(jsonPath("$.data.attendance.presentClasses").value(1))
                .andExpect(jsonPath("$.data.attendance.overallPercentage").value(100.0));

        // Student2 has 0 present out of 1 -> 0%
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendance.totalClasses").value(1))
                .andExpect(jsonPath("$.data.attendance.presentClasses").value(0))
                .andExpect(jsonPath("$.data.attendance.overallPercentage").value(0.0));
    }

    @Test
    @DisplayName("17. Exams belong only to enrolled courses")
    void testStudentExamsBelongOnlyToEnrolledCourses() throws Exception {
        // Student1 is enrolled in CS301 (ML) -> should see ML Mid Term, but not Cloud Final
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.upcomingExams", hasSize(1)))
                .andExpect(jsonPath("$.data.upcomingExams[0].examName").value("ML Mid Term"));
    }

    @Test
    @DisplayName("18. Marks belong only to authenticated student")
    void testStudentMarksBelongOnlyToAuthenticatedStudent() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marks.totalMarksRecorded").value(1))
                .andExpect(jsonPath("$.data.marks.marks[0].marksObtained").value(45.0))
                .andExpect(jsonPath("$.data.marks.marks[0].grade").value("A+"));
    }

    @Test
    @DisplayName("19. Timetable is relevant to student")
    void testStudentTimetableRelevant() throws Exception {
        // Student1 is in AIML, Semester 3 -> timetable entry matches
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timetable.totalEntries").value(1))
                .andExpect(jsonPath("$.data.timetable.todaySchedule[0].courseCode").value("CS301"));

        // Student2 is in DS, Semester 3 -> 0 timetable entries
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timetable.totalEntries").value(0));
    }

    @Test
    @DisplayName("20. Notices follow Phase 8 visibility rules")
    void testStudentNoticesFollowVisibilityRules() throws Exception {
        // Student1 (AIML): sees campus-wide notice + AIML workshop notice = 2 notices
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notices.totalActive").value(2));

        // Student2 (DS): sees only campus-wide notice = 1 notice
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notices.totalActive").value(1));
    }

    @Test
    @DisplayName("21. Document counts belong only to authenticated student")
    void testStudentDocumentCountsBelongOnlyToAuthenticatedStudent() throws Exception {
        // Student1 has 1 pending document request
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentSummary.totalRequests").value(1))
                .andExpect(jsonPath("$.data.documentSummary.pendingRequests").value(1));

        // Student2 has 0 requests
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentSummary.totalRequests").value(0))
                .andExpect(jsonPath("$.data.documentSummary.pendingRequests").value(0));
    }

    // =========================================================================
    // SECURITY TESTS
    // =========================================================================

    @Test
    @DisplayName("22. Unauthenticated admin dashboard -> 401")
    void testUnauthenticatedAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("23. Faculty accessing admin dashboard -> 403")
    void testFacultyAccessingAdminDashboardForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("24. Student accessing admin dashboard -> 403")
    void testStudentAccessingAdminDashboardForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("25. Student accessing faculty dashboard -> 403")
    void testStudentAccessingFacultyDashboardForbidden() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("26. Admin accessing student dashboard -> 403")
    void testAdminAccessingStudentDashboardForbidden() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // EDGE CASES
    // =========================================================================

    @Test
    @DisplayName("27. No attendance records -> 0% rather than error")
    void testNoAttendanceRecordsReturnsZeroPercent() throws Exception {
        attendanceRepository.deleteAll();

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceOverview.totalClasses").value(0))
                .andExpect(jsonPath("$.data.attendanceOverview.overallPercentage").value(0.0));

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendance.totalClasses").value(0))
                .andExpect(jsonPath("$.data.attendance.overallPercentage").value(0.0));
    }

    @Test
    @DisplayName("28. No marks -> safe empty/zero summary")
    void testNoMarksReturnsSafeEmptySummary() throws Exception {
        markRepository.deleteAll();

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.academicPerformance.marksCount").value(0))
                .andExpect(jsonPath("$.data.academicPerformance.averageMarks").value(0.0));

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marks.totalMarksRecorded").value(0))
                .andExpect(jsonPath("$.data.marks.averageMarks").value(0.0));
    }

    @Test
    @DisplayName("29. No upcoming exams -> empty list")
    void testNoUpcomingExamsReturnsEmptyList() throws Exception {
        markRepository.deleteAll();
        examRepository.deleteAll();

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.upcomingExams", hasSize(0)));

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.upcomingExams", hasSize(0)));
    }

    @Test
    @DisplayName("30. No notices -> safe empty summary")
    void testNoNoticesReturnsSafeEmptySummary() throws Exception {
        noticeRepository.deleteAll();

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeSummary.totalActive").value(0))
                .andExpect(jsonPath("$.data.noticeSummary.urgentCount").value(0));

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notices.totalActive").value(0))
                .andExpect(jsonPath("$.data.notices.urgentCount").value(0));
    }

    @Test
    @DisplayName("31. No document requests -> zero counts")
    void testNoDocumentRequestsReturnsZeroCounts() throws Exception {
        documentRequestRepository.deleteAll();

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentSummary.totalRequests").value(0))
                .andExpect(jsonPath("$.data.documentSummary.pendingRequests").value(0));

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentSummary.totalRequests").value(0))
                .andExpect(jsonPath("$.data.documentSummary.pendingRequests").value(0));
    }
}
