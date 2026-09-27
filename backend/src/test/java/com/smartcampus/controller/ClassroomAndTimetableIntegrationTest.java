package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.ClassroomRequest;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.request.TimetableRequest;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.CourseType;
import com.smartcampus.entity.enums.Role;
import com.smartcampus.entity.enums.RoomType;
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
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Classrooms and Timetable Management Integration Tests (Phase 7)")
class ClassroomAndTimetableIntegrationTest {

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
    @Autowired private DocumentRequestRepository documentRequestRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String adminToken;
    private String faculty1Token;
    private String faculty2Token;
    private String student1Token;
    private String student2Token;

    private Classroom classroom1;
    private Classroom classroom2;
    private Program program1;
    private Program program2;
    private Course course1;
    private Course course2;
    private Faculty faculty1;
    private Faculty faculty2;
    private Student student1;
    private Student student2;
    private Timetable timetable1;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

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
        userRepository.save(User.builder()
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

        // 3. Faculty 1 & Faculty 2
        User f1User = userRepository.save(User.builder()
                .username("faculty1")
                .email("faculty1@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        faculty1 = facultyRepository.save(Faculty.builder()
                .user(f1User)
                .department(dept)
                .employeeCode("FAC001")
                .firstName("John")
                .lastName("Doe")
                .designation("Associate Professor")
                .build());

        User f2User = userRepository.save(User.builder()
                .username("faculty2")
                .email("faculty2@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        faculty2 = facultyRepository.save(Faculty.builder()
                .user(f2User)
                .department(dept)
                .employeeCode("FAC002")
                .firstName("Jane")
                .lastName("Smith")
                .designation("Assistant Professor")
                .build());

        // 4. Students
        User s1User = userRepository.save(User.builder()
                .username("student1")
                .email("student1@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        student1 = studentRepository.save(Student.builder()
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

        student2 = studentRepository.save(Student.builder()
                .user(s2User)
                .program(program2)
                .rollNumber("23IOT001")
                .firstName("Bob")
                .lastName("Builder")
                .admissionYear(2023)
                .currentSemester(3)
                .section("A")
                .build());

        // 5. Courses
        course1 = courseRepository.save(Course.builder()
                .program(program1)
                .faculty(faculty1)
                .courseCode("CS301")
                .courseName("Data Structures")
                .credits(new BigDecimal("4.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        course2 = courseRepository.save(Course.builder()
                .program(program2)
                .faculty(faculty2)
                .courseCode("IT301")
                .courseName("Sensors & Actuators")
                .credits(new BigDecimal("4.0"))
                .semester(3)
                .courseType(CourseType.LAB)
                .build());

        // 6. Classrooms
        classroom1 = classroomRepository.save(Classroom.builder()
                .roomNumber("LH-101")
                .building("Aryabhatta Block")
                .roomType(RoomType.CLASSROOM)
                .capacity(60)
                .isActive(true)
                .build());

        classroom2 = classroomRepository.save(Classroom.builder()
                .roomNumber("LAB-201")
                .building("Turing Lab Complex")
                .roomType(RoomType.LAB)
                .capacity(30)
                .isActive(true)
                .build());

        // 7. Seed 1 Timetable entry
        timetable1 = timetableRepository.save(Timetable.builder()
                .program(program1)
                .course(course1)
                .faculty(faculty1)
                .classroom(classroom1)
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .semester(3)
                .academicYear("2024-2025")
                .build());

        // 8. Generate JWT tokens
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
    // CLASSROOM TESTS
    // ==========================================

    @Test
    @DisplayName("Admin can create a new classroom (201 Created)")
    void adminCanCreateClassroom() throws Exception {
        ClassroomRequest request = ClassroomRequest.builder()
                .roomNumber("SH-301")
                .building("Kalam Block")
                .roomType(RoomType.SEMINAR_HALL)
                .capacity(150)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/admin/classrooms")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.roomNumber", is("SH-301")))
                .andExpect(jsonPath("$.data.capacity", is(150)))
                .andExpect(jsonPath("$.data.roomType", is("SEMINAR_HALL")));
    }

    @Test
    @DisplayName("Admin can list classrooms with filters and pagination (200 OK)")
    void adminCanListClassroomsWithFilters() throws Exception {
        mockMvc.perform(get("/api/admin/classrooms")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("roomType", "CLASSROOM")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].roomNumber", is("LH-101")));
    }

    @Test
    @DisplayName("Admin can get classroom by ID (200 OK)")
    void adminCanGetClassroomById() throws Exception {
        mockMvc.perform(get("/api/admin/classrooms/{id}", classroom1.getClassroomId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.roomNumber", is("LH-101")));
    }

    @Test
    @DisplayName("Admin can update classroom (200 OK)")
    void adminCanUpdateClassroom() throws Exception {
        ClassroomRequest updateReq = ClassroomRequest.builder()
                .roomNumber("LH-101-UPDATED")
                .building("Aryabhatta Block A")
                .roomType(RoomType.CLASSROOM)
                .capacity(80)
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/admin/classrooms/{id}", classroom1.getClassroomId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.roomNumber", is("LH-101-UPDATED")))
                .andExpect(jsonPath("$.data.capacity", is(80)));
    }

    @Test
    @DisplayName("Admin can delete classroom not associated with timetable (200 OK)")
    void adminCanDeleteClassroom() throws Exception {
        mockMvc.perform(delete("/api/admin/classrooms/{id}", classroom2.getClassroomId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Admin cannot delete classroom associated with timetable entries (409 Conflict)")
    void cannotDeleteClassroomReferencedByTimetable() throws Exception {
        mockMvc.perform(delete("/api/admin/classrooms/{id}", classroom1.getClassroomId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Admin cannot create classroom with duplicate room number (409 Conflict)")
    void adminCannotCreateDuplicateRoomNumber() throws Exception {
        ClassroomRequest request = ClassroomRequest.builder()
                .roomNumber("LH-101")
                .building("Aryabhatta Block")
                .roomType(RoomType.CLASSROOM)
                .capacity(50)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/admin/classrooms")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Admin cannot update classroom to existing room number of another room (409 Conflict)")
    void adminCannotUpdateToDuplicateRoomNumber() throws Exception {
        ClassroomRequest updateReq = ClassroomRequest.builder()
                .roomNumber("LH-101")
                .building("Turing Lab Complex")
                .roomType(RoomType.LAB)
                .capacity(35)
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/admin/classrooms/{id}", classroom2.getClassroomId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Admin cannot create classroom with invalid capacity <= 0 (400 Bad Request)")
    void adminCannotCreateClassroomWithInvalidCapacity() throws Exception {
        ClassroomRequest request = ClassroomRequest.builder()
                .roomNumber("LH-999")
                .building("Aryabhatta Block")
                .roomType(RoomType.CLASSROOM)
                .capacity(0)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/admin/classrooms")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Faculty cannot manage classrooms (403 Forbidden)")
    void facultyCannotManageClassrooms() throws Exception {
        ClassroomRequest request = ClassroomRequest.builder()
                .roomNumber("LH-202")
                .building("Block B")
                .roomType(RoomType.CLASSROOM)
                .capacity(40)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/admin/classrooms")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/classrooms")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student cannot manage classrooms (403 Forbidden)")
    void studentCannotManageClassrooms() throws Exception {
        mockMvc.perform(get("/api/admin/classrooms")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/admin/classrooms/{id}", classroom2.getClassroomId())
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // TIMETABLE TESTS
    // ==========================================

    @Test
    @DisplayName("Admin can create timetable entry (201 Created)")
    void adminCanCreateTimetableEntry() throws Exception {
        TimetableRequest request = TimetableRequest.builder()
                .programId(program2.getProgramId())
                .courseId(course2.getCourseId())
                .facultyId(faculty2.getFacultyId())
                .classroomId(classroom2.getClassroomId())
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.courseCode", is("IT301")))
                .andExpect(jsonPath("$.data.dayOfWeek", is("MONDAY")));
    }

    @Test
    @DisplayName("Admin can list timetable entries with filters and pagination (200 OK)")
    void adminCanListTimetableWithFilters() throws Exception {
        mockMvc.perform(get("/api/admin/timetable")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("dayOfWeek", "MONDAY")
                        .param("semester", "3")
                        .param("academicYear", "2024-2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode", is("CS301")));
    }

    @Test
    @DisplayName("Admin can get timetable entry by ID (200 OK)")
    void adminCanGetTimetableById() throws Exception {
        mockMvc.perform(get("/api/admin/timetable/{id}", timetable1.getTimetableId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.courseCode", is("CS301")));
    }

    @Test
    @DisplayName("Admin can update timetable entry (200 OK)")
    void adminCanUpdateTimetableEntry() throws Exception {
        TimetableRequest updateReq = TimetableRequest.builder()
                .programId(program1.getProgramId())
                .courseId(course1.getCourseId())
                .facultyId(faculty1.getFacultyId())
                .classroomId(classroom1.getClassroomId())
                .dayOfWeek("TUESDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(put("/api/admin/timetable/{id}", timetable1.getTimetableId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.dayOfWeek", is("TUESDAY")));
    }

    @Test
    @DisplayName("Admin can delete timetable entry (200 OK)")
    void adminCanDeleteTimetableEntry() throws Exception {
        mockMvc.perform(delete("/api/admin/timetable/{id}", timetable1.getTimetableId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Faculty can read own timetable entries (200 OK)")
    void facultyCanReadOwnTimetable() throws Exception {
        // Faculty 1 has timetable1
        mockMvc.perform(get("/api/faculty/timetable")
                        .header("Authorization", "Bearer " + faculty1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseCode", is("CS301")));

        // Faculty 2 has no entries yet
        mockMvc.perform(get("/api/faculty/timetable")
                        .header("Authorization", "Bearer " + faculty2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Student can read own program timetable entries (200 OK)")
    void studentCanReadOwnTimetable() throws Exception {
        // Student 1 is in Program 1 (AIML)
        mockMvc.perform(get("/api/student/timetable")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].programCode", is("AIML")));

        // Student 2 is in Program 2 (IOT)
        mockMvc.perform(get("/api/student/timetable")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Faculty cannot create timetable entry (403 Forbidden)")
    void facultyCannotCreateTimetable() throws Exception {
        TimetableRequest request = TimetableRequest.builder()
                .programId(program1.getProgramId())
                .courseId(course1.getCourseId())
                .facultyId(faculty1.getFacultyId())
                .classroomId(classroom1.getClassroomId())
                .dayOfWeek("WEDNESDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + faculty1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student cannot create timetable entry (403 Forbidden)")
    void studentCannotCreateTimetable() throws Exception {
        TimetableRequest request = TimetableRequest.builder()
                .programId(program1.getProgramId())
                .courseId(course1.getCourseId())
                .facultyId(faculty1.getFacultyId())
                .classroomId(classroom1.getClassroomId())
                .dayOfWeek("WEDNESDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Timetable creation rejects invalid time range where startTime >= endTime (400 Bad Request)")
    void timetableCreationRejectsInvalidTimeRange() throws Exception {
        TimetableRequest request = TimetableRequest.builder()
                .programId(program2.getProgramId())
                .courseId(course2.getCourseId())
                .facultyId(faculty2.getFacultyId())
                .classroomId(classroom2.getClassroomId())
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(10, 0)) // startTime after endTime
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Timetable creation rejects classroom clash for overlapping time (409 Conflict)")
    void timetableCreationRejectsClassroomOverlap() throws Exception {
        // timetable1 is in classroom1 on MONDAY 09:00-10:00.
        // Try scheduling course2 with faculty2 in classroom1 overlapping 09:30-10:30
        TimetableRequest request = TimetableRequest.builder()
                .programId(program2.getProgramId())
                .courseId(course2.getCourseId())
                .facultyId(faculty2.getFacultyId())
                .classroomId(classroom1.getClassroomId()) // same classroom
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Classroom scheduling conflict")));
    }

    @Test
    @DisplayName("Timetable creation rejects faculty clash for overlapping time (409 Conflict)")
    void timetableCreationRejectsFacultyOverlap() throws Exception {
        // faculty1 is teaching timetable1 on MONDAY 09:00-10:00 in classroom1.
        // Try scheduling faculty1 in classroom2 on MONDAY 09:15-10:15
        TimetableRequest request = TimetableRequest.builder()
                .programId(program1.getProgramId())
                .courseId(course1.getCourseId())
                .facultyId(faculty1.getFacultyId()) // same faculty
                .classroomId(classroom2.getClassroomId()) // different classroom
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 15))
                .endTime(LocalTime.of(10, 15))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Faculty scheduling conflict")));
    }

    @Test
    @DisplayName("Timetable creation rejects program clash for overlapping time (409 Conflict)")
    void timetableCreationRejectsProgramOverlap() throws Exception {
        // program1 has timetable1 on MONDAY 09:00-10:00 in classroom1.
        // Try scheduling another class for program1 on MONDAY 09:30-10:30 with faculty2 in classroom2
        // First create another course in program1 taught by faculty2
        Course course1B = courseRepository.save(Course.builder()
                .program(program1)
                .faculty(faculty2)
                .courseCode("CS302")
                .courseName("Algorithms")
                .credits(new BigDecimal("4.0"))
                .semester(3)
                .courseType(CourseType.THEORY)
                .build());

        TimetableRequest request = TimetableRequest.builder()
                .programId(program1.getProgramId()) // same program
                .courseId(course1B.getCourseId())
                .facultyId(faculty2.getFacultyId())
                .classroomId(classroom2.getClassroomId())
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Program scheduling conflict")));
    }

    @Test
    @DisplayName("Timetable allows valid adjacent classes (e.g. 09:00-10:00 and 10:00-11:00) (201 Created)")
    void timetableAllowsValidAdjacentClasses() throws Exception {
        // timetable1 is on MONDAY 09:00-10:00 in classroom1.
        // Schedule back-to-back class in classroom1 10:00-11:00 for program2/faculty2
        TimetableRequest request = TimetableRequest.builder()
                .programId(program2.getProgramId())
                .courseId(course2.getCourseId())
                .facultyId(faculty2.getFacultyId())
                .classroomId(classroom1.getClassroomId()) // same classroom
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(10, 0)) // exactly adjacent start
                .endTime(LocalTime.of(11, 0))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(post("/api/admin/timetable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.startTime", is("10:00:00")))
                .andExpect(jsonPath("$.data.endTime", is("11:00:00")));
    }

    @Test
    @DisplayName("Timetable update allows updating own entry without self-conflict (200 OK)")
    void timetableUpdateAllowsSameEntryWithoutSelfConflict() throws Exception {
        TimetableRequest updateReq = TimetableRequest.builder()
                .programId(program1.getProgramId())
                .courseId(course1.getCourseId())
                .facultyId(faculty1.getFacultyId())
                .classroomId(classroom1.getClassroomId())
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .semester(3)
                .academicYear("2024-2025")
                .build();

        mockMvc.perform(put("/api/admin/timetable/{id}", timetable1.getTimetableId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Unauthenticated user cannot access classrooms or timetable (401 Unauthorized)")
    void unauthenticatedUserCannotAccessEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/classrooms"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/timetable"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/faculty/timetable"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/student/timetable"))
                .andExpect(status().isUnauthorized());
    }
}
