package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.ExamRequest;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.request.MarkRequest;
import com.smartcampus.dto.request.MarkUpdateRequest;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.CourseType;
import com.smartcampus.entity.enums.EnrollmentStatus;
import com.smartcampus.entity.enums.ExamType;
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
@DisplayName("Exams and Marks Management Integration Tests (Phase 6)")
class ExamAndMarkIntegrationTest {

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
    private Exam exam1;
    private Exam exam2;
    private Mark mark1;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        timetableRepository.deleteAll();
        documentRequestRepository.deleteAll();
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

        // 1. Admin
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
                .description("CSE Dept")
                .build());

        Program prog = programRepository.save(Program.builder()
                .department(dept)
                .programCode("AIML")
                .programName("AIML Program")
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
                .specialization("AI")
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
                .specialization("Cloud")
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

        // 7. Course 1 (taught by faculty1) & Course 2 (taught by faculty2)
        course1 = courseRepository.save(Course.builder()
                .program(prog)
                .faculty(faculty1)
                .courseCode("CS301")
                .courseName("Operating Systems")
                .credits(new BigDecimal("3.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        course2 = courseRepository.save(Course.builder()
                .program(prog)
                .faculty(faculty2)
                .courseCode("CS302")
                .courseName("Database Systems")
                .credits(new BigDecimal("4.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        // 8. Enrollments: Student1 enrolled in Course1 & Course2; Student2 enrolled ONLY in Course2
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

        enrollmentRepository.save(Enrollment.builder()
                .student(student2)
                .course(course2)
                .academicYear("2024-2025")
                .semester(3)
                .enrollmentDate(LocalDate.of(2024, 8, 12))
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // 9. Initial Exam 1 (Course 1, faculty 1) & Exam 2 (Course 2, faculty 2)
        exam1 = examRepository.save(Exam.builder()
                .course(course1)
                .examName("Mid Term 1")
                .examType(ExamType.MID_1)
                .examDate(LocalDate.of(2024, 10, 15))
                .maxMarks(new BigDecimal("30.00"))
                .build());

        exam2 = examRepository.save(Exam.builder()
                .course(course2)
                .examName("Mid Term 1")
                .examType(ExamType.MID_1)
                .examDate(LocalDate.of(2024, 10, 16))
                .maxMarks(new BigDecimal("30.00"))
                .build());

        // 10. Initial Mark 1 (Student1 in Exam 1, scored 27/30, grade A)
        mark1 = markRepository.save(Mark.builder()
                .exam(exam1)
                .student(student1)
                .marksObtained(new BigDecimal("27.00"))
                .grade("A")
                .enteredBy(faculty1)
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
    // EXAM TESTS: ADMIN & FACULTY & STUDENT
    // =========================================================================

    @Test
    @DisplayName("Admin: list exams with pagination metadata")
    void testAdminListExams() throws Exception {
        mockMvc.perform(get("/api/admin/exams")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("Admin: get exam by ID")
    void testAdminGetExamById() throws Exception {
        mockMvc.perform(get("/api/admin/exams/" + exam1.getExamId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.examName").value("Mid Term 1"))
                .andExpect(jsonPath("$.data.examType").value("MID_1"))
                .andExpect(jsonPath("$.data.courseCode").value("CS301"));
    }

    @Test
    @DisplayName("Admin: create exam with valid fields")
    void testAdminCreateExam() throws Exception {
        ExamRequest request = ExamRequest.builder()
                .courseId(course1.getCourseId())
                .examName("Mid Term 2")
                .examType(ExamType.MID_2)
                .examDate(LocalDate.of(2024, 11, 20))
                .maxMarks(new BigDecimal("30.00"))
                .build();

        mockMvc.perform(post("/api/admin/exams")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.examName").value("Mid Term 2"))
                .andExpect(jsonPath("$.data.examType").value("MID_2"));
    }

    @Test
    @DisplayName("Admin: update exam successfully")
    void testAdminUpdateExam() throws Exception {
        ExamRequest request = ExamRequest.builder()
                .courseId(course1.getCourseId())
                .examName("Updated Mid Term 1")
                .examType(ExamType.MID_1)
                .examDate(LocalDate.of(2024, 10, 18))
                .maxMarks(new BigDecimal("40.00"))
                .build();

        mockMvc.perform(put("/api/admin/exams/" + exam1.getExamId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.examName").value("Updated Mid Term 1"))
                .andExpect(jsonPath("$.data.maxMarks").value(40.0));
    }

    @Test
    @DisplayName("Admin: delete exam fails if marks recorded (409 Conflict)")
    void testAdminDeleteExamFailsWhenMarksExist() throws Exception {
        // exam1 has mark1 recorded
        mockMvc.perform(delete("/api/admin/exams/" + exam1.getExamId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Faculty: create exam for own course succeeds (201)")
    void testFacultyCreateOwnCourseExam() throws Exception {
        ExamRequest request = ExamRequest.builder()
                .courseId(course1.getCourseId())
                .examName("Semester End Lab")
                .examType(ExamType.LAB)
                .examDate(LocalDate.of(2024, 12, 1))
                .maxMarks(new BigDecimal("50.00"))
                .build();

        mockMvc.perform(post("/api/faculty/exams")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.examName").value("Semester End Lab"));
    }

    @Test
    @DisplayName("Faculty: create exam for another faculty's course returns 403 Forbidden")
    void testFacultyCannotCreateExamForAnotherFacultyCourse() throws Exception {
        // faculty1 tries to create exam for course2 (taught by faculty2)
        ExamRequest request = ExamRequest.builder()
                .courseId(course2.getCourseId())
                .examName("Unauthorized Exam")
                .examType(ExamType.INTERNAL)
                .examDate(LocalDate.of(2024, 11, 1))
                .maxMarks(new BigDecimal("20.00"))
                .build();

        mockMvc.perform(post("/api/faculty/exams")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Faculty: view exams returns only exams for assigned courses")
    void testFacultyViewOwnCourseExams() throws Exception {
        mockMvc.perform(get("/api/faculty/exams")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode").value("CS301"));
    }

    @Test
    @DisplayName("Student: view exams returns only exams for enrolled courses")
    void testStudentViewEnrolledCourseExams() throws Exception {
        // student2 is enrolled ONLY in course2 (exam2)
        mockMvc.perform(get("/api/student/exams")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode").value("CS302"));
    }

    // =========================================================================
    // MARKS TESTS: ADMIN & FACULTY & STUDENT
    // =========================================================================

    @Test
    @DisplayName("Faculty: record mark for own course exam succeeds (201)")
    void testFacultyRecordMarkOwnCourse() throws Exception {
        // student1 is enrolled in course1, exam1 is in course1 (taught by faculty1)
        // Mark Student1 in another exam or seed Exam for test
        Exam mid2 = examRepository.save(Exam.builder()
                .course(course1)
                .examName("Mid Term 2")
                .examType(ExamType.MID_2)
                .examDate(LocalDate.of(2024, 11, 20))
                .maxMarks(new BigDecimal("30.00"))
                .build());

        MarkRequest request = MarkRequest.builder()
                .examId(mid2.getExamId())
                .studentId(student1.getStudentId())
                .marksObtained(new BigDecimal("28.50"))
                .grade("A+")
                .build();

        mockMvc.perform(post("/api/faculty/marks")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.marksObtained").value(28.5))
                .andExpect(jsonPath("$.data.grade").value("A+"))
                .andExpect(jsonPath("$.data.studentRollNumber").value("25071A6601"));
    }

    @Test
    @DisplayName("Faculty: record mark for another faculty's course exam returns 403 Forbidden")
    void testFacultyCannotRecordMarkForAnotherFacultyExam() throws Exception {
        // exam2 is course2 (taught by faculty2). Faculty1 attempts to enter marks.
        MarkRequest request = MarkRequest.builder()
                .examId(exam2.getExamId())
                .studentId(student2.getStudentId())
                .marksObtained(new BigDecimal("25.00"))
                .build();

        mockMvc.perform(post("/api/faculty/marks")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Faculty: update mark for own course exam succeeds (200)")
    void testFacultyUpdateOwnCourseMark() throws Exception {
        MarkUpdateRequest request = MarkUpdateRequest.builder()
                .marksObtained(new BigDecimal("29.00"))
                .grade("A+")
                .build();

        mockMvc.perform(put("/api/faculty/marks/" + mark1.getMarkId())
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marksObtained").value(29.0))
                .andExpect(jsonPath("$.data.grade").value("A+"));
    }

    @Test
    @DisplayName("Student: view own marks returns only own marks")
    void testStudentViewOwnMarks() throws Exception {
        mockMvc.perform(get("/api/student/marks")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].studentRollNumber").value("25071A6601"))
                .andExpect(jsonPath("$.data.content[0].marksObtained").value(27.0));

        // student2 has no marks yet
        mockMvc.perform(get("/api/student/marks")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Student: view results returns course-level breakdown and percentage")
    void testStudentViewResults() throws Exception {
        mockMvc.perform(get("/api/student/results")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].courseCode").value("CS301"))
                .andExpect(jsonPath("$.data[0].totalExams").value(1))
                .andExpect(jsonPath("$.data[0].totalMarksObtained").value(27.0))
                .andExpect(jsonPath("$.data[0].totalMaxMarks").value(30.0))
                .andExpect(jsonPath("$.data[0].percentage").value(90.0))
                .andExpect(jsonPath("$.data[0].examResults", hasSize(1)))
                .andExpect(jsonPath("$.data[0].examResults[0].grade").value("A"));
    }

    // =========================================================================
    // VALIDATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Validation: student not enrolled in course returns 409 Conflict")
    void testMarkFailsWhenStudentNotEnrolled() throws Exception {
        // student2 is NOT enrolled in course1 (exam1)
        MarkRequest request = MarkRequest.builder()
                .examId(exam1.getExamId())
                .studentId(student2.getStudentId())
                .marksObtained(new BigDecimal("20.00"))
                .build();

        mockMvc.perform(post("/api/admin/marks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("not enrolled")));
    }

    @Test
    @DisplayName("Validation: duplicate mark for same student and exam returns 409 Conflict")
    void testDuplicateMarkReturns409() throws Exception {
        // student1 already has mark1 in exam1
        MarkRequest request = MarkRequest.builder()
                .examId(exam1.getExamId())
                .studentId(student1.getStudentId())
                .marksObtained(new BigDecimal("22.00"))
                .build();

        mockMvc.perform(post("/api/admin/marks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already recorded")));
    }

    @Test
    @DisplayName("Validation: marks obtained exceeding maxMarks returns 400 Bad Request")
    void testMarksExceedingMaxMarksReturns400() throws Exception {
        // exam2 maxMarks is 30.00. Marks obtained = 35.00
        MarkRequest request = MarkRequest.builder()
                .examId(exam2.getExamId())
                .studentId(student1.getStudentId()) // student1 is enrolled in course2
                .marksObtained(new BigDecimal("35.00"))
                .build();

        mockMvc.perform(post("/api/admin/marks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("cannot exceed maximum marks")));
    }

    @Test
    @DisplayName("Validation: marks obtained below 0 returns 400 Bad Request")
    void testMarksBelowZeroReturns400() throws Exception {
        MarkRequest request = MarkRequest.builder()
                .examId(exam2.getExamId())
                .studentId(student1.getStudentId())
                .marksObtained(new BigDecimal("-5.00"))
                .build();

        mockMvc.perform(post("/api/admin/marks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // =========================================================================
    // SECURITY TESTS
    // =========================================================================

    @Test
    @DisplayName("Security: unauthenticated requests return 401 Unauthorized")
    void testUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/exams")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/marks")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/faculty/exams")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/student/exams")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/student/marks")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/student/results")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: students blocked from modifying exams and marks (403 Forbidden)")
    void testStudentBlockedFromModifyingExamsAndMarks() throws Exception {
        mockMvc.perform(post("/api/faculty/exams")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/faculty/marks")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/exams")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/marks")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
