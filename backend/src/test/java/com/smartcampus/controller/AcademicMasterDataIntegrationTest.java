package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.*;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.CourseType;
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
@DisplayName("Academic Master Data Integration Tests (Phase 3)")
class AcademicMasterDataIntegrationTest {

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
    private String studentToken;

    private Department testDept;
    private Program testProg;
    private Faculty testFaculty;
    private Student testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clear in reverse FK order
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

        // 1. Seed ADMIN user
        User adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        // 2. Seed Department
        testDept = departmentRepository.save(Department.builder()
                .departmentCode("CSE")
                .departmentName("Computer Science and Engineering")
                .description("Department of CSE")
                .build());

        // 3. Seed Program
        testProg = programRepository.save(Program.builder()
                .department(testDept)
                .programCode("AIML")
                .programName("Artificial Intelligence & Machine Learning")
                .durationYears(4)
                .build());

        // 4. Seed Faculty User & Profile
        User facultyUser = userRepository.save(User.builder()
                .username("sagar_y")
                .email("sagar_y@vnrvjiet.in")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        testFaculty = facultyRepository.save(Faculty.builder()
                .user(facultyUser)
                .department(testDept)
                .employeeCode("FAC001")
                .firstName("Sagar")
                .lastName("Y")
                .designation("Assistant Professor")
                .specialization("Machine Learning")
                .phone("9876543210")
                .build());

        // 5. Seed Student User & Profile
        User studentUser = userRepository.save(User.builder()
                .username("25071A6601")
                .email("25071A6601@vnrvjiet.in")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        testStudent = studentRepository.save(Student.builder()
                .user(studentUser)
                .program(testProg)
                .rollNumber("25071A6601")
                .firstName("John")
                .lastName("Doe")
                .gender("Male")
                .dateOfBirth(LocalDate.of(2004, 5, 15))
                .admissionYear(2025)
                .currentSemester(1)
                .section("A")
                .phone("9123456789")
                .build());

        // 6. Seed Course
        testCourse = courseRepository.save(Course.builder()
                .program(testProg)
                .faculty(testFaculty)
                .courseCode("CS101")
                .courseName("Introduction to AI")
                .credits(new BigDecimal("4.0"))
                .semester(1)
                .courseType(CourseType.THEORY)
                .build());

        // Obtain JWT tokens for each role
        adminToken = obtainAccessToken("admin", "Password@123");
        facultyToken = obtainAccessToken("sagar_y", "Password@123");
        studentToken = obtainAccessToken("25071A6601", "Password@123");
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

    // ==========================================
    // 1. DEPARTMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Department: List all departments as ADMIN")
    void testListDepartments_Admin() throws Exception {
        mockMvc.perform(get("/api/admin/departments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].departmentCode", is("CSE")));
    }

    @Test
    @DisplayName("Department: Get department by ID as ADMIN")
    void testGetDepartmentById_Admin() throws Exception {
        mockMvc.perform(get("/api/admin/departments/" + testDept.getDepartmentId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.departmentCode", is("CSE")))
                .andExpect(jsonPath("$.data.departmentName", is("Computer Science and Engineering")));
    }

    @Test
    @DisplayName("Department: Get department by non-existent ID returns 404")
    void testGetDepartmentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/admin/departments/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Department: Create new department as ADMIN")
    void testCreateDepartment_Success() throws Exception {
        DepartmentRequest request = DepartmentRequest.builder()
                .departmentCode("ECE")
                .departmentName("Electronics and Communication Engineering")
                .description("ECE Department")
                .build();

        mockMvc.perform(post("/api/admin/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.departmentCode", is("ECE")))
                .andExpect(jsonPath("$.data.departmentId", notNullValue()));
    }

    @Test
    @DisplayName("Department: Create duplicate department code returns 409 Conflict")
    void testCreateDepartment_DuplicateCode() throws Exception {
        DepartmentRequest request = DepartmentRequest.builder()
                .departmentCode("CSE")
                .departmentName("Another Computer Science")
                .build();

        mockMvc.perform(post("/api/admin/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    @DisplayName("Department: Update department as ADMIN")
    void testUpdateDepartment_Success() throws Exception {
        DepartmentRequest request = DepartmentRequest.builder()
                .departmentCode("CSE")
                .departmentName("Department of Computer Science & Eng")
                .description("Updated description")
                .build();

        mockMvc.perform(put("/api/admin/departments/" + testDept.getDepartmentId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.departmentName", is("Department of Computer Science & Eng")));
    }

    @Test
    @DisplayName("Department: Delete department referenced by programs returns 409 Conflict")
    void testDeleteDepartment_ReferencedConstraint() throws Exception {
        mockMvc.perform(delete("/api/admin/departments/" + testDept.getDepartmentId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("programs are associated")));
    }

    @Test
    @DisplayName("Department: Delete isolated department with no children succeeds")
    void testDeleteDepartment_Success() throws Exception {
        Department isolated = departmentRepository.save(Department.builder()
                .departmentCode("MECH")
                .departmentName("Mechanical Engineering")
                .build());

        mockMvc.perform(delete("/api/admin/departments/" + isolated.getDepartmentId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // ==========================================
    // 2. PROGRAM TESTS
    // ==========================================

    @Test
    @DisplayName("Program: List all programs as ADMIN")
    void testListPrograms_Admin() throws Exception {
        mockMvc.perform(get("/api/admin/programs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].programCode", is("AIML")));
    }

    @Test
    @DisplayName("Program: List programs by department ID")
    void testListProgramsByDepartment() throws Exception {
        mockMvc.perform(get("/api/admin/departments/" + testDept.getDepartmentId() + "/programs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].departmentId", is(testDept.getDepartmentId().intValue())));
    }

    @Test
    @DisplayName("Program: Create new program as ADMIN")
    void testCreateProgram_Success() throws Exception {
        ProgramRequest request = ProgramRequest.builder()
                .departmentId(testDept.getDepartmentId())
                .programCode("CSE-IoT")
                .programName("Internet of Things")
                .durationYears(4)
                .build();

        mockMvc.perform(post("/api/admin/programs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.programCode", is("CSE-IoT")));
    }

    @Test
    @DisplayName("Program: Create program with invalid department returns 404")
    void testCreateProgram_InvalidDepartment() throws Exception {
        ProgramRequest request = ProgramRequest.builder()
                .departmentId(999999L)
                .programCode("NEW-PROG")
                .programName("New Program")
                .durationYears(4)
                .build();

        mockMvc.perform(post("/api/admin/programs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Program: Update program as ADMIN")
    void testUpdateProgram_Success() throws Exception {
        ProgramRequest request = ProgramRequest.builder()
                .departmentId(testDept.getDepartmentId())
                .programCode("AIML")
                .programName("AI & Machine Learning Engineering")
                .durationYears(4)
                .build();

        mockMvc.perform(put("/api/admin/programs/" + testProg.getProgramId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.programName", is("AI & Machine Learning Engineering")));
    }

    @Test
    @DisplayName("Program: Delete program with enrolled students returns 409 Conflict")
    void testDeleteProgram_ReferencedConstraint() throws Exception {
        mockMvc.perform(delete("/api/admin/programs/" + testProg.getProgramId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("students are enrolled")));
    }

    // ==========================================
    // 3. STUDENT TESTS
    // ==========================================

    @Test
    @DisplayName("Student: List students with pagination as ADMIN")
    void testListStudents_Admin() throws Exception {
        mockMvc.perform(get("/api/admin/students?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.content[0].rollNumber", is("25071A6601")));
    }

    @Test
    @DisplayName("Student: Filter by program, semester, and section")
    void testStudentFiltering() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .param("programId", testProg.getProgramId().toString())
                        .param("semester", "1")
                        .param("section", "A")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].rollNumber", is("25071A6601")));
    }

    @Test
    @DisplayName("Student: Search by roll number or name")
    void testStudentSearch() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .param("search", "John")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].firstName", is("John")));
    }

    @Test
    @DisplayName("Student: Create student creates User identity transactionally")
    void testCreateStudent_Success() throws Exception {
        StudentRequest request = StudentRequest.builder()
                .programId(testProg.getProgramId())
                .rollNumber("25071A6602")
                .firstName("Alice")
                .lastName("Smith")
                .gender("Female")
                .dateOfBirth(LocalDate.of(2004, 8, 20))
                .admissionYear(2025)
                .currentSemester(1)
                .section("A")
                .phone("9888877777")
                .build();

        mockMvc.perform(post("/api/admin/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.rollNumber", is("25071A6602")))
                .andExpect(jsonPath("$.data.username", is("25071A6602")))
                .andExpect(jsonPath("$.data.email", is("25071a6602@vnrvjiet.in")))
                .andExpect(jsonPath("$.data.studentId", notNullValue()));

        // Verify created User can authenticate with default password
        String newStudentToken = obtainAccessToken("25071A6602", "Password@123");
        assertNotNull(newStudentToken);
    }

    @Test
    @DisplayName("Student: Create duplicate student roll number returns 409 Conflict")
    void testCreateStudent_DuplicateRollNumber() throws Exception {
        StudentRequest request = StudentRequest.builder()
                .programId(testProg.getProgramId())
                .rollNumber("25071A6601")
                .firstName("Duplicate")
                .admissionYear(2025)
                .currentSemester(1)
                .section("A")
                .build();

        mockMvc.perform(post("/api/admin/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    @DisplayName("Student: Student accesses own profile successfully")
    void testStudentOwnProfile_Success() throws Exception {
        mockMvc.perform(get("/api/student/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.rollNumber", is("25071A6601")))
                .andExpect(jsonPath("$.data.firstName", is("John")));
    }

    // ==========================================
    // 4. FACULTY TESTS
    // ==========================================

    @Test
    @DisplayName("Faculty: List faculty with pagination as ADMIN")
    void testListFaculty_Admin() throws Exception {
        mockMvc.perform(get("/api/admin/faculty?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].employeeCode", is("FAC001")));
    }

    @Test
    @DisplayName("Faculty: Create faculty creates User identity with email prefix username")
    void testCreateFaculty_Success() throws Exception {
        FacultyRequest request = FacultyRequest.builder()
                .departmentId(testDept.getDepartmentId())
                .employeeCode("FAC002")
                .firstName("Sandhya")
                .lastName("N")
                .designation("Associate Professor")
                .specialization("Deep Learning")
                .email("sandhya_n@vnrvjiet.in")
                .phone("9988776655")
                .build();

        mockMvc.perform(post("/api/admin/faculty")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.employeeCode", is("FAC002")))
                .andExpect(jsonPath("$.data.username", is("sandhya_n")))
                .andExpect(jsonPath("$.data.email", is("sandhya_n@vnrvjiet.in")));

        // Verify newly created faculty can log in
        String newFacultyToken = obtainAccessToken("sandhya_n", "Password@123");
        assertNotNull(newFacultyToken);
    }

    @Test
    @DisplayName("Faculty: Create duplicate employee code returns 409 Conflict")
    void testCreateFaculty_DuplicateEmployeeCode() throws Exception {
        FacultyRequest request = FacultyRequest.builder()
                .departmentId(testDept.getDepartmentId())
                .employeeCode("FAC001")
                .firstName("Duplicate")
                .email("diff_email@vnrvjiet.in")
                .build();

        mockMvc.perform(post("/api/admin/faculty")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("Faculty: Faculty accesses own profile successfully")
    void testFacultyOwnProfile_Success() throws Exception {
        mockMvc.perform(get("/api/faculty/profile")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.employeeCode", is("FAC001")))
                .andExpect(jsonPath("$.data.username", is("sagar_y")));
    }

    // ==========================================
    // 5. COURSE TESTS
    // ==========================================

    @Test
    @DisplayName("Course: List courses with pagination as ADMIN")
    void testListCourses_Admin() throws Exception {
        mockMvc.perform(get("/api/admin/courses?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].courseCode", is("CS101")));
    }

    @Test
    @DisplayName("Course: Create course with valid program and faculty")
    void testCreateCourse_Success() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .programId(testProg.getProgramId())
                .facultyId(testFaculty.getFacultyId())
                .courseCode("CS102")
                .courseName("Data Structures")
                .credits(new BigDecimal("3.0"))
                .semester(2)
                .courseType(CourseType.THEORY)
                .build();

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.courseCode", is("CS102")));
    }

    @Test
    @DisplayName("Course: Create course with invalid program returns 404")
    void testCreateCourse_InvalidProgram() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .programId(999999L)
                .courseCode("CS999")
                .courseName("Invalid Prog Course")
                .credits(new BigDecimal("3.0"))
                .semester(1)
                .courseType(CourseType.THEORY)
                .build();

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Course: Create course with invalid faculty returns 404")
    void testCreateCourse_InvalidFaculty() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .programId(testProg.getProgramId())
                .facultyId(999999L)
                .courseCode("CS998")
                .courseName("Invalid Faculty Course")
                .credits(new BigDecimal("3.0"))
                .semester(1)
                .courseType(CourseType.THEORY)
                .build();

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Course: Create duplicate course code returns 409 Conflict")
    void testCreateCourse_DuplicateCode() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .programId(testProg.getProgramId())
                .courseCode("CS101")
                .courseName("Duplicate Course")
                .credits(new BigDecimal("3.0"))
                .semester(1)
                .courseType(CourseType.THEORY)
                .build();

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    // ==========================================
    // 6. ROLE AUTHORIZATION & SECURITY
    // ==========================================

    @Test
    @DisplayName("Security: FACULTY blocked from admin student CRUD (403 Forbidden)")
    void testFacultyBlockedFromAdminStudentCRUD() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("Security: STUDENT blocked from admin student CRUD (403 Forbidden)")
    void testStudentBlockedFromAdminStudentCRUD() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("Security: FACULTY blocked from admin department creation (403 Forbidden)")
    void testFacultyBlockedFromAdminDepartmentCRUD() throws Exception {
        DepartmentRequest request = DepartmentRequest.builder()
                .departmentCode("NEW")
                .departmentName("Unauthorized Dept")
                .build();

        mockMvc.perform(post("/api/admin/departments")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Security: STUDENT blocked from admin faculty endpoints (403 Forbidden)")
    void testStudentBlockedFromAdminFacultyCRUD() throws Exception {
        mockMvc.perform(get("/api/admin/faculty")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Security: Unauthenticated request to protected endpoints returns 401 Unauthorized")
    void testUnauthenticatedBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        mockMvc.perform(get("/api/student/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Security: Faculty attempting to access student profile endpoint returns 403 Forbidden")
    void testFacultyBlockedFromStudentProfile() throws Exception {
        mockMvc.perform(get("/api/student/profile")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    private void assertNotNull(String value) {
        org.junit.jupiter.api.Assertions.assertNotNull(value);
    }
}
