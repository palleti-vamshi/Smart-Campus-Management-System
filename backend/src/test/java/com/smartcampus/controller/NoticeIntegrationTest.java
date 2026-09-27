package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.request.NoticeRequest;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Notice Centre Integration Tests (Phase 8)")
class NoticeIntegrationTest {

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
    @Autowired private DocumentRequestRepository documentRequestRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String adminToken;
    private String faculty1Token;
    private String faculty2Token;
    private String student1Token;
    private String student2Token;

    private User adminUser;
    private User faculty1User;
    private User faculty2User;
    private Program program1;
    private Program program2;

    private Notice deptNotice;
    private Notice prog1Notice;
    private Notice prog2Notice;
    private Notice expiredNotice;
    private Notice futureNotice;
    private Notice f1Notice;
    private Notice f2Notice;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        noticeRepository.deleteAll();
        timetableRepository.deleteAll();
        classroomRepository.deleteAll();
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
        adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        // 2. Department & Programs
        Department dept = departmentRepository.save(Department.builder()
                .departmentCode("CSE")
                .departmentName("Computer Science and Engineering")
                .build());

        program1 = programRepository.save(Program.builder()
                .department(dept)
                .programCode("AIML")
                .programName("B.Tech in Artificial Intelligence & Machine Learning")
                .durationYears(4)
                .build());

        program2 = programRepository.save(Program.builder()
                .department(dept)
                .programCode("IOT")
                .programName("B.Tech in Internet of Things")
                .durationYears(4)
                .build());

        // 3. Faculty 1 & 2
        faculty1User = userRepository.save(User.builder()
                .username("faculty1")
                .email("faculty1@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        facultyRepository.save(Faculty.builder()
                .user(faculty1User)
                .department(dept)
                .employeeCode("FAC001")
                .firstName("John")
                .lastName("Doe")
                .designation("Professor")
                .build());

        faculty2User = userRepository.save(User.builder()
                .username("faculty2")
                .email("faculty2@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        facultyRepository.save(Faculty.builder()
                .user(faculty2User)
                .department(dept)
                .employeeCode("FAC002")
                .firstName("Jane")
                .lastName("Smith")
                .designation("Associate Professor")
                .build());

        // 4. Students
        User s1User = userRepository.save(User.builder()
                .username("student1")
                .email("student1@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        studentRepository.save(Student.builder()
                .user(s1User)
                .program(program1)
                .rollNumber("23AIML001")
                .firstName("Alice")
                .lastName("Wonder")
                .admissionYear(2023)
                .currentSemester(3)
                .section("A")
                .build());

        User s2User = userRepository.save(User.builder()
                .username("student2")
                .email("student2@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        studentRepository.save(Student.builder()
                .user(s2User)
                .program(program2)
                .rollNumber("23IOT001")
                .firstName("Bob")
                .lastName("Builder")
                .admissionYear(2023)
                .currentSemester(3)
                .section("A")
                .build());

        // 5. Seed notices
        LocalDateTime now = LocalDateTime.now();

        deptNotice = noticeRepository.save(Notice.builder()
                .title("Annual Tech Symposium")
                .content("Campus-wide technical festival announced.")
                .category(NoticeCategory.EVENT)
                .priority(NoticePriority.HIGH)
                .targetProgram(null) // department-wide
                .publishedBy(adminUser)
                .publishAt(now.minusHours(2))
                .expiresAt(now.plusDays(7))
                .build());

        prog1Notice = noticeRepository.save(Notice.builder()
                .title("AIML Mid Term Examination Schedule")
                .content("Mid-term exam schedule for Semester 3 AIML.")
                .category(NoticeCategory.EXAM)
                .priority(NoticePriority.URGENT)
                .targetProgram(program1)
                .publishedBy(faculty1User)
                .publishAt(now.minusHours(1))
                .expiresAt(now.plusDays(5))
                .build());

        prog2Notice = noticeRepository.save(Notice.builder()
                .title("IoT Hardware Lab Update")
                .content("Lab 201 open for sensor calibration.")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.NORMAL)
                .targetProgram(program2)
                .publishedBy(faculty2User)
                .publishAt(now.minusHours(1))
                .expiresAt(now.plusDays(5))
                .build());

        expiredNotice = noticeRepository.save(Notice.builder()
                .title("Old Bonafide Deadline")
                .content("Bonafide applications closed.")
                .category(NoticeCategory.GENERAL)
                .priority(NoticePriority.NORMAL)
                .targetProgram(program1)
                .publishedBy(adminUser)
                .publishAt(now.minusDays(5))
                .expiresAt(now.minusDays(1)) // expired
                .build());

        futureNotice = noticeRepository.save(Notice.builder()
                .title("Future Hackathon 2025")
                .content("Details will be published next week.")
                .category(NoticeCategory.EVENT)
                .priority(NoticePriority.NORMAL)
                .targetProgram(program1)
                .publishedBy(adminUser)
                .publishAt(now.plusDays(2)) // future
                .expiresAt(now.plusDays(10))
                .build());

        f1Notice = noticeRepository.save(Notice.builder()
                .title("Faculty 1 Project Guidance")
                .content("Project review session on Friday.")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.NORMAL)
                .targetProgram(null)
                .publishedBy(faculty1User)
                .publishAt(now.minusHours(1))
                .expiresAt(now.plusDays(3))
                .build());

        f2Notice = noticeRepository.save(Notice.builder()
                .title("Faculty 2 Research Internship")
                .content("Call for research interns in IoT.")
                .category(NoticeCategory.INTERNSHIP)
                .priority(NoticePriority.HIGH)
                .targetProgram(null)
                .publishedBy(faculty2User)
                .publishAt(now.minusHours(1))
                .expiresAt(now.plusDays(3))
                .build());

        // 6. Obtain tokens
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

    // ==========================================
    // ADMIN NOTICE TESTS
    // ==========================================

    @Test
    @DisplayName("Admin can create a notice (200 OK)")
    void adminCanCreateNotice() throws Exception {
        NoticeRequest request = NoticeRequest.builder()
                .title("Admin Notice: Holiday Announcement")
                .content("Campus will remain closed on Monday.")
                .category(NoticeCategory.GENERAL)
                .priority(NoticePriority.NORMAL)
                .targetProgramId(null)
                .publishAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/api/admin/notices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Admin Notice: Holiday Announcement")))
                .andExpect(jsonPath("$.data.active", is(true)));
    }

    @Test
    @DisplayName("Admin can get all notices with pagination (200 OK)")
    void adminCanGetAllNotices() throws Exception {
        mockMvc.perform(get("/api/admin/notices")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(7)));
    }

    @Test
    @DisplayName("Admin can get notice by ID (200 OK)")
    void adminCanGetNoticeById() throws Exception {
        mockMvc.perform(get("/api/admin/notices/{id}", deptNotice.getNoticeId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Annual Tech Symposium")));
    }

    @Test
    @DisplayName("Admin can update any notice (200 OK)")
    void adminCanUpdateNotice() throws Exception {
        NoticeRequest updateReq = NoticeRequest.builder()
                .title("Updated Annual Tech Symposium")
                .content("Updated content with guest speaker details.")
                .category(NoticeCategory.EVENT)
                .priority(NoticePriority.URGENT)
                .targetProgramId(null)
                .publishAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();

        mockMvc.perform(put("/api/admin/notices/{id}", deptNotice.getNoticeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Updated Annual Tech Symposium")))
                .andExpect(jsonPath("$.data.priority", is("URGENT")));
    }

    @Test
    @DisplayName("Admin can delete notice (200 OK)")
    void adminCanDeleteNotice() throws Exception {
        mockMvc.perform(delete("/api/admin/notices/{id}", expiredNotice.getNoticeId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Admin create notice with non-existent target program fails (404 Not Found)")
    void adminCreateNoticeInvalidTargetProgramFails() throws Exception {
        NoticeRequest request = NoticeRequest.builder()
                .title("Invalid Target Notice")
                .content("Invalid program test.")
                .category(NoticeCategory.GENERAL)
                .priority(NoticePriority.NORMAL)
                .targetProgramId(999999L)
                .publishAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/admin/notices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Admin create notice with expiry date before publish date fails (400 Bad Request)")
    void adminCreateNoticeInvalidExpiryDateFails() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        NoticeRequest request = NoticeRequest.builder()
                .title("Invalid Expiry Notice")
                .content("Expires before publishing.")
                .category(NoticeCategory.GENERAL)
                .priority(NoticePriority.NORMAL)
                .publishAt(now.plusDays(2))
                .expiresAt(now) // before publishAt
                .build();

        mockMvc.perform(post("/api/admin/notices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ==========================================
    // FACULTY NOTICE TESTS
    // ==========================================

    @Test
    @DisplayName("Faculty can create a notice (200 OK)")
    void facultyCanCreateNotice() throws Exception {
        NoticeRequest request = NoticeRequest.builder()
                .title("Faculty Announcement")
                .content("Lab submissions due tomorrow.")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.HIGH)
                .targetProgramId(program1.getProgramId())
                .publishAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/faculty/notices")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.publishedByUserId", is(faculty1User.getUserId().intValue())));
    }

    @Test
    @DisplayName("Faculty can get notices (200 OK)")
    void facultyCanGetNotices() throws Exception {
        mockMvc.perform(get("/api/faculty/notices")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Faculty can update own notice (200 OK)")
    void facultyCanUpdateOwnNotice() throws Exception {
        NoticeRequest updateReq = NoticeRequest.builder()
                .title("Faculty 1 Project Guidance (Rescheduled)")
                .content("Session shifted to Monday.")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.HIGH)
                .publishAt(LocalDateTime.now().minusHours(1))
                .build();

        mockMvc.perform(put("/api/faculty/notices/{id}", f1Notice.getNoticeId())
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Faculty 1 Project Guidance (Rescheduled)")));
    }

    @Test
    @DisplayName("Faculty can delete own notice (200 OK)")
    void facultyCanDeleteOwnNotice() throws Exception {
        mockMvc.perform(delete("/api/faculty/notices/{id}", f1Notice.getNoticeId())
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Faculty cannot update another faculty's notice (403 Forbidden)")
    void facultyCannotUpdateAnotherFacultyNotice() throws Exception {
        // Faculty 2 tries to update f1Notice
        NoticeRequest updateReq = NoticeRequest.builder()
                .title("Malicious Edit")
                .content("Unauthorized change.")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.NORMAL)
                .publishAt(LocalDateTime.now())
                .build();

        mockMvc.perform(put("/api/faculty/notices/{id}", f1Notice.getNoticeId())
                        .header("Authorization", "Bearer " + faculty2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Faculty cannot delete another faculty's notice (403 Forbidden)")
    void facultyCannotDeleteAnotherFacultyNotice() throws Exception {
        // Faculty 2 tries to delete f1Notice
        mockMvc.perform(delete("/api/faculty/notices/{id}", f1Notice.getNoticeId())
                        .header("Authorization", "Bearer " + faculty2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ==========================================
    // STUDENT NOTICE TESTS
    // ==========================================

    @Test
    @DisplayName("Student can view active department-wide and own program notices (200 OK)")
    void studentCanViewActiveRelevantNotices() throws Exception {
        // Student 1 is in Program 1 (AIML)
        // Should see: deptNotice, prog1Notice, f1Notice, f2Notice (since f1 & f2 are dept-wide)
        // Should NOT see: prog2Notice (IOT), expiredNotice (expired), futureNotice (future)
        mockMvc.perform(get("/api/student/notices")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[*].title", hasItems(
                        "Annual Tech Symposium",
                        "AIML Mid Term Examination Schedule",
                        "Faculty 1 Project Guidance",
                        "Faculty 2 Research Internship"
                )))
                .andExpect(jsonPath("$.data.content[*].title", not(hasItems(
                        "IoT Hardware Lab Update",
                        "Old Bonafide Deadline",
                        "Future Hackathon 2025"
                ))));
    }

    @Test
    @DisplayName("Student 2 (IOT) sees IoT notices but not AIML notices (200 OK)")
    void student2SeesOwnProgramNotices() throws Exception {
        mockMvc.perform(get("/api/student/notices")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[*].title", hasItem("IoT Hardware Lab Update")))
                .andExpect(jsonPath("$.data.content[*].title", not(hasItem("AIML Mid Term Examination Schedule"))));
    }

    @Test
    @DisplayName("Student cannot directly access another program's notice by ID (403 Forbidden)")
    void studentCannotAccessAnotherProgramNoticeById() throws Exception {
        // Student 1 (AIML) tries to access prog2Notice (IOT)
        mockMvc.perform(get("/api/student/notices/{id}", prog2Notice.getNoticeId())
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Student cannot directly access expired notice by ID (403 Forbidden)")
    void studentCannotAccessExpiredNoticeById() throws Exception {
        mockMvc.perform(get("/api/student/notices/{id}", expiredNotice.getNoticeId())
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Student cannot directly access future notice by ID (403 Forbidden)")
    void studentCannotAccessFutureNoticeById() throws Exception {
        mockMvc.perform(get("/api/student/notices/{id}", futureNotice.getNoticeId())
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Student can filter notices by category (200 OK)")
    void studentCanFilterByCategory() throws Exception {
        mockMvc.perform(get("/api/student/notices")
                        .header("Authorization", "Bearer " + student1Token)
                        .param("category", "EXAM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].category", is("EXAM")));
    }

    @Test
    @DisplayName("Student can filter notices by priority (200 OK)")
    void studentCanFilterByPriority() throws Exception {
        mockMvc.perform(get("/api/student/notices")
                        .header("Authorization", "Bearer " + student1Token)
                        .param("priority", "URGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].priority", is("URGENT")));
    }

    @Test
    @DisplayName("Student can search notices by keyword (200 OK)")
    void studentCanSearchByKeyword() throws Exception {
        mockMvc.perform(get("/api/student/notices")
                        .header("Authorization", "Bearer " + student1Token)
                        .param("keyword", "symposium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", containsString("Symposium")));
    }

    @Test
    @DisplayName("Student pagination works correctly (200 OK)")
    void studentPaginationWorks() throws Exception {
        mockMvc.perform(get("/api/student/notices")
                        .header("Authorization", "Bearer " + student1Token)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.data.pageSize", is(2)));
    }

    // ==========================================
    // ROLE SECURITY TESTS
    // ==========================================

    @Test
    @DisplayName("Student accessing admin notice endpoint returns 403 Forbidden")
    void studentAccessingAdminEndpointReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/notices")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Faculty accessing admin notice endpoint returns 403 Forbidden")
    void facultyAccessingAdminEndpointReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/notices")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student accessing faculty notice endpoint returns 403 Forbidden")
    void studentAccessingFacultyEndpointReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/faculty/notices")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin accessing student notice endpoint returns 403 Forbidden")
    void adminAccessingStudentEndpointReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/student/notices")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated user returns 401 Unauthorized")
    void unauthenticatedUserReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/notices"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/faculty/notices"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/student/notices"))
                .andExpect(status().isUnauthorized());
    }
}
