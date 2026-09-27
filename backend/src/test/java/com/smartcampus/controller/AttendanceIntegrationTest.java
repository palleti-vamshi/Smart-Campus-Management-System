package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.AttendanceRequest;
import com.smartcampus.dto.request.AttendanceUpdateRequest;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.AttendanceStatus;
import com.smartcampus.entity.enums.CourseType;
import com.smartcampus.entity.enums.EnrollmentStatus;
import com.smartcampus.entity.enums.Role;
import com.smartcampus.repository.*;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Attendance Management Integration Tests (Phase 5)")
class AttendanceIntegrationTest {

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
    @Autowired private MarkRepository markRepository;
    @Autowired private DocumentRequestRepository documentRequestRepository;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String adminToken;
    private String faculty1Token;
    private String faculty2Token;
    private String student1Token;
    private String student2Token;

    private Student student1;
    private Student student2;
    private Faculty faculty1;
    private Faculty faculty2;
    private Course course1;
    private Course course2;
    private Attendance attendance1;
    private Attendance attendance2;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        timetableRepository.deleteAll();
        documentRequestRepository.deleteAll();
        markRepository.deleteAll();
        attendanceRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
        programRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        // Admin User
        userRepository.save(User.builder()
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        // Department & Program
        Department dept = departmentRepository.save(Department.builder()
                .departmentCode("CSE")
                .departmentName("Computer Science and Engineering")
                .description("CSE Dept")
                .build());

        Program prog = programRepository.save(Program.builder()
                .department(dept)
                .programCode("AIML")
                .programName("AIML Program")
                .durationYears(4)
                .build());

        // Faculty 1
        User fUser1 = userRepository.save(User.builder()
                .username("sagar_y")
                .email("sagar_y@vnrvjiet.in")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        faculty1 = facultyRepository.save(Faculty.builder()
                .user(fUser1)
                .department(dept)
                .employeeCode("FAC001")
                .firstName("Sagar")
                .lastName("Y")
                .designation("Assistant Professor")
                .specialization("AI")
                .phone("9876543210")
                .build());

        // Faculty 2
        User fUser2 = userRepository.save(User.builder()
                .username("priya_r")
                .email("priya_r@vnrvjiet.in")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        faculty2 = facultyRepository.save(Faculty.builder()
                .user(fUser2)
                .department(dept)
                .employeeCode("FAC002")
                .firstName("Priya")
                .lastName("R")
                .designation("Associate Professor")
                .specialization("Cloud")
                .phone("9876543211")
                .build());

        // Student 1
        User sUser1 = userRepository.save(User.builder()
                .username("25071A6601")
                .email("25071A6601@vnrvjiet.in")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        student1 = studentRepository.save(Student.builder()
                .user(sUser1)
                .program(prog)
                .rollNumber("25071A6601")
                .firstName("Aarav")
                .lastName("Sharma")
                .gender("Male")
                .dateOfBirth(LocalDate.of(2005, 6, 15))
                .admissionYear(2023)
                .currentSemester(3)
                .section("A")
                .phone("9123456789")
                .build());

        // Student 2
        User sUser2 = userRepository.save(User.builder()
                .username("25071A6602")
                .email("25071A6602@vnrvjiet.in")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        student2 = studentRepository.save(Student.builder()
                .user(sUser2)
                .program(prog)
                .rollNumber("25071A6602")
                .firstName("Ananya")
                .lastName("Reddy")
                .gender("Female")
                .dateOfBirth(LocalDate.of(2005, 8, 20))
                .admissionYear(2023)
                .currentSemester(3)
                .section("B")
                .phone("9123456780")
                .build());

        // Course 1 (taught by faculty1)
        course1 = courseRepository.save(Course.builder()
                .program(prog)
                .faculty(faculty1)
                .courseCode("CS301")
                .courseName("Operating Systems")
                .credits(new BigDecimal("3.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        // Course 2 (taught by faculty2)
        course2 = courseRepository.save(Course.builder()
                .program(prog)
                .faculty(faculty2)
                .courseCode("CS302")
                .courseName("Database Systems")
                .credits(new BigDecimal("4.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        // Enrollments:
        // Student1 is enrolled in Course1 and Course2
        enrollmentRepository.save(Enrollment.builder()
                .student(student1)
                .course(course1)
                .academicYear("2024-2025")
                .semester(3)
                .enrollmentDate(LocalDate.of(2024, 8, 12))
                .status(EnrollmentStatus.ACTIVE)
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .student(student1)
                .course(course2)
                .academicYear("2024-2025")
                .semester(3)
                .enrollmentDate(LocalDate.of(2024, 8, 12))
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // Student2 is enrolled ONLY in Course2
        enrollmentRepository.save(Enrollment.builder()
                .student(student2)
                .course(course2)
                .academicYear("2024-2025")
                .semester(3)
                .enrollmentDate(LocalDate.of(2024, 8, 12))
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // Seed initial attendance records:
        // Attendance1: Student1 in Course1 on 2024-09-01 (PRESENT, marked by faculty1)
        attendance1 = attendanceRepository.save(Attendance.builder()
                .student(student1)
                .course(course1)
                .attendanceDate(LocalDate.of(2024, 9, 1))
                .status(AttendanceStatus.PRESENT)
                .markedBy(faculty1)
                .build());

        // Attendance2: Student2 in Course2 on 2024-09-01 (ABSENT, marked by faculty2)
        attendance2 = attendanceRepository.save(Attendance.builder()
                .student(student2)
                .course(course2)
                .attendanceDate(LocalDate.of(2024, 9, 1))
                .status(AttendanceStatus.ABSENT)
                .markedBy(faculty2)
                .build());

        // Tokens
        adminToken = obtainAccessToken("admin", "Password@123");
        faculty1Token = obtainAccessToken("sagar_y", "Password@123");
        faculty2Token = obtainAccessToken("priya_r", "Password@123");
        student1Token = obtainAccessToken("25071A6601", "Password@123");
        student2Token = obtainAccessToken("25071A6602", "Password@123");
    }

    private String obtainAccessToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("data").get("token").asText();
    }

    // =========================================================================
    // ADMIN ENDPOINT TESTS
    // =========================================================================

    @Test
    @DisplayName("Admin: list all attendance records with pagination metadata")
    void testAdminListAttendance() throws Exception {
        mockMvc.perform(get("/api/admin/attendance")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20));
    }

    @Test
    @DisplayName("Admin: get single attendance by ID")
    void testAdminGetAttendanceById() throws Exception {
        mockMvc.perform(get("/api/admin/attendance/" + attendance1.getAttendanceId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendanceId").value(attendance1.getAttendanceId()))
                .andExpect(jsonPath("$.data.studentRollNumber").value("25071A6601"))
                .andExpect(jsonPath("$.data.courseCode").value("CS301"))
                .andExpect(jsonPath("$.data.attendanceDate").value("2024-09-01"))
                .andExpect(jsonPath("$.data.status").value("PRESENT"));
    }

    @Test
    @DisplayName("Admin: filter attendance records by courseId, studentId, and status")
    void testAdminFilterAttendance() throws Exception {
        mockMvc.perform(get("/api/admin/attendance")
                        .param("courseId", course1.getCourseId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode").value("CS301"));

        mockMvc.perform(get("/api/admin/attendance")
                        .param("status", "ABSENT")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].studentRollNumber").value("25071A6602"));
    }

    @Test
    @DisplayName("Admin: record attendance successfully for any course")
    void testAdminRecordAttendance() throws Exception {
        AttendanceRequest request = AttendanceRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course1.getCourseId())
                .attendanceDate(LocalDate.of(2024, 9, 2))
                .status(AttendanceStatus.PRESENT)
                .build();

        mockMvc.perform(post("/api/admin/attendance")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentRollNumber").value("25071A6601"))
                .andExpect(jsonPath("$.data.courseCode").value("CS301"))
                .andExpect(jsonPath("$.data.status").value("PRESENT"));
    }

    @Test
    @DisplayName("Admin: update attendance status and date")
    void testAdminUpdateAttendance() throws Exception {
        AttendanceUpdateRequest request = AttendanceUpdateRequest.builder()
                .attendanceDate(LocalDate.of(2024, 9, 5))
                .status(AttendanceStatus.LATE)
                .build();

        mockMvc.perform(put("/api/admin/attendance/" + attendance1.getAttendanceId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendanceDate").value("2024-09-05"))
                .andExpect(jsonPath("$.data.status").value("LATE"));
    }

    @Test
    @DisplayName("Admin: delete attendance successfully")
    void testAdminDeleteAttendance() throws Exception {
        mockMvc.perform(delete("/api/admin/attendance/" + attendance1.getAttendanceId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/admin/attendance/" + attendance1.getAttendanceId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // FACULTY ENDPOINT TESTS & OWNERSHIP RULES
    // =========================================================================

    @Test
    @DisplayName("Faculty: record attendance for own course succeeds (201)")
    void testFacultyRecordAttendanceOwnCourse() throws Exception {
        // Faculty1 teaches Course1, student1 is enrolled
        AttendanceRequest request = AttendanceRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course1.getCourseId())
                .attendanceDate(LocalDate.of(2024, 9, 2))
                .status(AttendanceStatus.PRESENT)
                .build();

        mockMvc.perform(post("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.courseCode").value("CS301"))
                .andExpect(jsonPath("$.data.markedByFacultyName").value("Sagar Y"));
    }

    @Test
    @DisplayName("Faculty: record attendance for another faculty's course returns 403 Forbidden")
    void testFacultyCannotRecordAttendanceForAnotherFacultyCourse() throws Exception {
        // Faculty1 tries to mark attendance for Course2 (taught by faculty2)
        AttendanceRequest request = AttendanceRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course2.getCourseId())
                .attendanceDate(LocalDate.of(2024, 9, 2))
                .status(AttendanceStatus.PRESENT)
                .build();

        mockMvc.perform(post("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Faculty: view attendance records only returns courses assigned to this faculty")
    void testFacultyViewOwnCourseAttendance() throws Exception {
        // Faculty1 teaches Course1 (has attendance1). Course2 has attendance2 (taught by faculty2).
        mockMvc.perform(get("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode").value("CS301"));

        // Faculty1 cannot query attendance of Course2 even by passing courseId parameter
        mockMvc.perform(get("/api/faculty/attendance")
                        .param("courseId", course2.getCourseId().toString())
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Faculty: update attendance for own course succeeds (200)")
    void testFacultyUpdateOwnCourseAttendance() throws Exception {
        // Faculty1 teaches course1 (attendance1)
        AttendanceUpdateRequest request = AttendanceUpdateRequest.builder()
                .status(AttendanceStatus.LATE)
                .build();

        mockMvc.perform(put("/api/faculty/attendance/" + attendance1.getAttendanceId())
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LATE"));
    }

    @Test
    @DisplayName("Faculty: update attendance for another faculty's course returns 403 Forbidden")
    void testFacultyCannotUpdateAnotherFacultyAttendance() throws Exception {
        // Faculty1 tries to update attendance2 (Course2, taught by faculty2)
        AttendanceUpdateRequest request = AttendanceUpdateRequest.builder()
                .status(AttendanceStatus.PRESENT)
                .build();

        mockMvc.perform(put("/api/faculty/attendance/" + attendance2.getAttendanceId())
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Faculty: delete attendance for own course succeeds (200)")
    void testFacultyDeleteOwnCourseAttendance() throws Exception {
        mockMvc.perform(delete("/api/faculty/attendance/" + attendance1.getAttendanceId())
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Faculty: delete attendance for another faculty's course returns 403 Forbidden")
    void testFacultyCannotDeleteAnotherFacultyAttendance() throws Exception {
        mockMvc.perform(delete("/api/faculty/attendance/" + attendance2.getAttendanceId())
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // STUDENT ENDPOINT TESTS & ATTENDANCE SUMMARY
    // =========================================================================

    @Test
    @DisplayName("Student: view own attendance records returns 200")
    void testStudentViewOwnAttendance() throws Exception {
        // student1Token has attendance1 (in Course1)
        mockMvc.perform(get("/api/student/attendance")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].studentRollNumber").value("25071A6601"));
    }

    @Test
    @DisplayName("Student: cannot view another student's attendance records")
    void testStudentCannotViewAnotherStudentAttendance() throws Exception {
        // student2Token should only see attendance2
        mockMvc.perform(get("/api/student/attendance")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].studentRollNumber").value("25071A6602"));
    }

    @Test
    @DisplayName("Student: view attendance summary per course with percentages and zero-class safety")
    void testStudentAttendanceSummary() throws Exception {
        // Student1 is enrolled in Course1 (1 class, 1 PRESENT) and Course2 (0 classes)
        mockMvc.perform(get("/api/student/attendance/summary")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                // Verify Course1: 1 class, 1 present, 100%
                .andExpect(jsonPath("$.data[?(@.courseCode == 'CS301')].totalClasses").value(hasItem(1)))
                .andExpect(jsonPath("$.data[?(@.courseCode == 'CS301')].presentCount").value(hasItem(1)))
                .andExpect(jsonPath("$.data[?(@.courseCode == 'CS301')].absentCount").value(hasItem(0)))
                .andExpect(jsonPath("$.data[?(@.courseCode == 'CS301')].attendancePercentage").value(hasItem(100.0)))
                // Verify Course2: 0 classes, 0%, no division-by-zero error
                .andExpect(jsonPath("$.data[?(@.courseCode == 'CS302')].totalClasses").value(hasItem(0)))
                .andExpect(jsonPath("$.data[?(@.courseCode == 'CS302')].attendancePercentage").value(hasItem(0.0)));
    }

    @Test
    @DisplayName("Student: blocked from modifying attendance (403 Forbidden)")
    void testStudentBlockedFromModifyingAttendance() throws Exception {
        mockMvc.perform(post("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/faculty/attendance/" + attendance1.getAttendanceId())
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/attendance")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // VALIDATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Validation: student not enrolled in course returns 409 Conflict")
    void testAttendanceFailsWhenStudentNotEnrolled() throws Exception {
        // Student2 is NOT enrolled in Course1 (CS301)
        AttendanceRequest request = AttendanceRequest.builder()
                .studentId(student2.getStudentId())
                .courseId(course1.getCourseId())
                .attendanceDate(LocalDate.of(2024, 9, 2))
                .status(AttendanceStatus.PRESENT)
                .build();

        mockMvc.perform(post("/api/admin/attendance")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("not enrolled")));
    }

    @Test
    @DisplayName("Validation: duplicate attendance on same date returns 409 Conflict")
    void testAttendanceDuplicateDateReturns409() throws Exception {
        // attendance1 already exists for student1 in course1 on 2024-09-01
        AttendanceRequest request = AttendanceRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course1.getCourseId())
                .attendanceDate(LocalDate.of(2024, 9, 1))
                .status(AttendanceStatus.PRESENT)
                .build();

        mockMvc.perform(post("/api/admin/attendance")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already recorded")));
    }

    @Test
    @DisplayName("Validation: invalid date range (startDate > endDate) returns 400 Bad Request")
    void testInvalidDateRangeReturns400() throws Exception {
        mockMvc.perform(get("/api/admin/attendance")
                        .param("startDate", "2024-09-10")
                        .param("endDate", "2024-09-01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // =========================================================================
    // SECURITY TESTS
    // =========================================================================

    @Test
    @DisplayName("Security: unauthenticated requests return 401")
    void testUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/attendance"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/faculty/attendance"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/student/attendance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: wrong role returns 403")
    void testWrongRoleReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/attendance")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/attendance")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }
}
