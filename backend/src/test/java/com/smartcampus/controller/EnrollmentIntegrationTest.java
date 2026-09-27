package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.EnrollmentRequest;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.entity.*;
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
@DisplayName("Enrollment Management Integration Tests (Phase 4)")
class EnrollmentIntegrationTest {

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
    private String facultyToken;
    private String otherFacultyToken;
    private String studentToken;
    private String otherStudentToken;

    private Student student1;
    private Student student2;
    private Course course1;
    private Course course2;
    private Faculty faculty1;
    private Faculty faculty2;
    private Enrollment enrollment1;
    private Enrollment enrollment2;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean tables in reverse foreign key order
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

        // 1. Admin User
        userRepository.save(User.builder()
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        // 2. Department & Program
        Department dept = departmentRepository.save(Department.builder()
                .departmentCode("CSE")
                .departmentName("Computer Science and Engineering")
                .description("Department of CSE")
                .build());

        Program prog = programRepository.save(Program.builder()
                .department(dept)
                .programCode("AIML")
                .programName("Artificial Intelligence & Machine Learning")
                .durationYears(4)
                .build());

        // 3. Faculty 1
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
                .specialization("Machine Learning")
                .phone("9876543210")
                .build());

        // 4. Faculty 2
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
                .specialization("Cloud Computing")
                .phone("9876543211")
                .build());

        // 5. Student 1
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

        // 6. Student 2
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

        // 7. Course 1 (taught by faculty1)
        course1 = courseRepository.save(Course.builder()
                .program(prog)
                .faculty(faculty1)
                .courseCode("CS301")
                .courseName("Operating Systems")
                .credits(new BigDecimal("3.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        // 8. Course 2 (taught by faculty2)
        course2 = courseRepository.save(Course.builder()
                .program(prog)
                .faculty(faculty2)
                .courseCode("CS302")
                .courseName("Database Management Systems")
                .credits(new BigDecimal("4.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        // 9. Initial Seed Enrollments
        enrollment1 = enrollmentRepository.save(Enrollment.builder()
                .student(student1)
                .course(course1)
                .academicYear("2024-2025")
                .semester(3)
                .enrollmentDate(LocalDate.of(2024, 8, 12))
                .status(EnrollmentStatus.ACTIVE)
                .build());

        enrollment2 = enrollmentRepository.save(Enrollment.builder()
                .student(student2)
                .course(course2)
                .academicYear("2024-2025")
                .semester(3)
                .enrollmentDate(LocalDate.of(2024, 8, 12))
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // Obtain JWT tokens
        adminToken = obtainAccessToken("admin", "Password@123");
        facultyToken = obtainAccessToken("sagar_y", "Password@123");
        otherFacultyToken = obtainAccessToken("priya_r", "Password@123");
        studentToken = obtainAccessToken("25071A6601", "Password@123");
        otherStudentToken = obtainAccessToken("25071A6602", "Password@123");
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
    @DisplayName("Admin: list all enrollments with pagination metadata")
    void testAdminListEnrollments() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20));
    }

    @Test
    @DisplayName("Admin: get single enrollment by ID")
    void testAdminGetEnrollmentById() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments/" + enrollment1.getEnrollmentId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enrollmentId").value(enrollment1.getEnrollmentId()))
                .andExpect(jsonPath("$.data.studentRollNumber").value("25071A6601"))
                .andExpect(jsonPath("$.data.studentName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.data.courseCode").value("CS301"))
                .andExpect(jsonPath("$.data.academicYear").value("2024-2025"))
                .andExpect(jsonPath("$.data.semester").value(3))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Admin: get non-existent enrollment returns 404")
    void testAdminGetNonExistentEnrollmentReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Admin: filter enrollments by studentId")
    void testAdminFilterByStudentId() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments")
                        .param("studentId", student1.getStudentId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].studentRollNumber").value("25071A6601"));
    }

    @Test
    @DisplayName("Admin: filter enrollments by courseId")
    void testAdminFilterByCourseId() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments")
                        .param("courseId", course2.getCourseId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode").value("CS302"));
    }

    @Test
    @DisplayName("Admin: filter enrollments by academicYear and semester")
    void testAdminFilterByAcademicYearAndSemester() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments")
                        .param("academicYear", "2024-2025")
                        .param("semester", "3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    @DisplayName("Admin: filter enrollments by status")
    void testAdminFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments")
                        .param("status", "ACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));

        mockMvc.perform(get("/api/admin/enrollments")
                        .param("status", "DROPPED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Admin: create enrollment successfully")
    void testAdminCreateEnrollment() throws Exception {
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course2.getCourseId())
                .academicYear("2024-2025")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentRollNumber").value("25071A6601"))
                .andExpect(jsonPath("$.data.courseCode").value("CS302"))
                .andExpect(jsonPath("$.data.academicYear").value("2024-2025"))
                .andExpect(jsonPath("$.data.semester").value(3))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Admin: create enrollment with duplicate (student, course, year, term) returns 409 Conflict")
    void testAdminCreateDuplicateEnrollmentReturns409() throws Exception {
        // enrollment1 already has student1 in course1 for 2024-2025 semester 3
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course1.getCourseId())
                .academicYear("2024-2025")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already enrolled")));
    }

    @Test
    @DisplayName("Admin: create enrollment with non-existent student returns 404")
    void testAdminCreateEnrollmentInvalidStudentReturns404() throws Exception {
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(99999L)
                .courseId(course1.getCourseId())
                .academicYear("2024-2025")
                .semester(3)
                .build();

        mockMvc.perform(post("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Admin: create enrollment with non-existent course returns 404")
    void testAdminCreateEnrollmentInvalidCourseReturns404() throws Exception {
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(99999L)
                .academicYear("2024-2025")
                .semester(3)
                .build();

        mockMvc.perform(post("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Admin: create enrollment with invalid semester returns 400")
    void testAdminCreateEnrollmentInvalidSemesterReturns400() throws Exception {
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course1.getCourseId())
                .academicYear("2024-2025")
                .semester(9) // Semester must be between 1 and 8
                .build();

        mockMvc.perform(post("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Admin: update enrollment status and semester successfully")
    void testAdminUpdateEnrollment() throws Exception {
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course1.getCourseId())
                .academicYear("2024-2025")
                .semester(4)
                .status(EnrollmentStatus.COMPLETED)
                .build();

        mockMvc.perform(put("/api/admin/enrollments/" + enrollment1.getEnrollmentId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.semester").value(4))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Admin: update enrollment to collide with another enrollment returns 409 Conflict")
    void testAdminUpdateEnrollmentCollisionReturns409() throws Exception {
        // Attempt to update enrollment2 to match enrollment1's (student1, course1, 2024-2025, 3)
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(student1.getStudentId())
                .courseId(course1.getCourseId())
                .academicYear("2024-2025")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/admin/enrollments/" + enrollment2.getEnrollmentId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Admin: delete enrollment successfully")
    void testAdminDeleteEnrollment() throws Exception {
        mockMvc.perform(delete("/api/admin/enrollments/" + enrollment1.getEnrollmentId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/admin/enrollments/" + enrollment1.getEnrollmentId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // STUDENT ENDPOINT TESTS
    // =========================================================================

    @Test
    @DisplayName("Student: view own enrollments returns 200 and only student's courses")
    void testStudentViewOwnEnrollments() throws Exception {
        mockMvc.perform(get("/api/student/enrollments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].studentRollNumber").value("25071A6601"))
                .andExpect(jsonPath("$.data.content[0].courseCode").value("CS301"));
    }

    @Test
    @DisplayName("Student: cannot view another student's enrollments even if querying with different filters")
    void testStudentCannotAccessAnotherStudentEnrollments() throws Exception {
        // studentToken belongs to 25071A6601; student2 has enrollment in CS302
        mockMvc.perform(get("/api/student/enrollments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].studentRollNumber", not(hasItem("25071A6602"))));
    }

    @Test
    @DisplayName("Student: blocked from admin enrollments CRUD endpoints (403 Forbidden)")
    void testStudentBlockedFromAdminEnrollments() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // FACULTY ENDPOINT TESTS
    // =========================================================================

    @Test
    @DisplayName("Faculty: view enrollments for taught courses returns 200")
    void testFacultyViewOwnCourseEnrollments() throws Exception {
        // faculty1 teaches course1 (CS301), which has enrollment1
        mockMvc.perform(get("/api/faculty/enrollments")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode").value("CS301"))
                .andExpect(jsonPath("$.data.content[0].studentRollNumber").value("25071A6601"));
    }

    @Test
    @DisplayName("Faculty: cannot view enrollments of courses taught by another faculty")
    void testFacultyCannotViewOtherFacultyEnrollments() throws Exception {
        // facultyToken is faculty1, course2 is taught by faculty2
        mockMvc.perform(get("/api/faculty/enrollments")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].courseCode", not(hasItem("CS302"))));

        // If faculty1 tries to specify courseId = course2
        mockMvc.perform(get("/api/faculty/enrollments")
                        .param("courseId", course2.getCourseId().toString())
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Faculty: blocked from admin enrollments CRUD endpoints (403 Forbidden)")
    void testFacultyBlockedFromAdminEnrollments() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/enrollments")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/admin/enrollments/" + enrollment1.getEnrollmentId())
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // SECURITY TESTS
    // =========================================================================

    @Test
    @DisplayName("Security: unauthenticated request to /api/admin/enrollments returns 401 Unauthorized")
    void testUnauthenticatedAdminEnrollmentsReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: unauthenticated request to /api/student/enrollments returns 401 Unauthorized")
    void testUnauthenticatedStudentEnrollmentsReturns401() throws Exception {
        mockMvc.perform(get("/api/student/enrollments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: unauthenticated request to /api/faculty/enrollments returns 401 Unauthorized")
    void testUnauthenticatedFacultyEnrollmentsReturns401() throws Exception {
        mockMvc.perform(get("/api/faculty/enrollments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: faculty attempting to access /api/student/enrollments returns 403 Forbidden")
    void testFacultyAccessingStudentEnrollmentsReturns403() throws Exception {
        mockMvc.perform(get("/api/student/enrollments")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: student attempting to access /api/faculty/enrollments returns 403 Forbidden")
    void testStudentAccessingFacultyEnrollmentsReturns403() throws Exception {
        mockMvc.perform(get("/api/faculty/enrollments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
