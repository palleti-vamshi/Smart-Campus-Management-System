package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.*;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Faculty Section-Scoped Access Control Integration Tests")
class FacultySectionSecurityIntegrationTest {

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
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String preetyToken;
    private String sayeedaToken;

    private Faculty preetyFaculty;
    private Faculty sayeedaFaculty;
    private Course javaTheoryCourse;
    private Course javaLabCourse;
    private Course dbmsCourse;

    private Student studentSecA;
    private Student studentSecB;
    private Student studentSecC;

    private Exam javaExam1;
    private Exam javaExam2;
    private Exam dbmsExam;

    private Mark markSecA;
    private Mark markSecB;

    private static final String SEED_HASH = "$2a$10$/vqkFH.7kR5J9g1ym.jGrefcoQO0YVp7HxwX38fg7YMC.2V1O3aeK"; // Password@123

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean tables in reverse dependency order
        markRepository.deleteAll();
        examRepository.deleteAll();
        attendanceRepository.deleteAll();
        enrollmentRepository.deleteAll();
        timetableRepository.deleteAll();
        classroomRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
        programRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Department & Program
        Department cseDept = departmentRepository.save(Department.builder()
                .departmentName("Computer Science & Engineering")
                .departmentCode("CSE")
                .description("CSE Department")
                .build());

        Program aimlProg = programRepository.save(Program.builder()
                .department(cseDept)
                .programName("Artificial Intelligence and Machine Learning")
                .programCode("AIML")
                .durationYears(4)
                .build());

        // 2. Users & Faculty
        User preetyUser = userRepository.save(User.builder()
                .username("preety_s")
                .email("preety_s@vnrvjiet.in")
                .passwordHash(SEED_HASH)
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        preetyFaculty = facultyRepository.save(Faculty.builder()
                .user(preetyUser)
                .department(cseDept)
                .employeeCode("FAC022")
                .firstName("Preety")
                .lastName("Singh")
                .designation("Assistant Professor")
                .specialization("JAVA & Object Oriented Systems")
                .phone("9876543222")
                .build());

        User sayeedaUser = userRepository.save(User.builder()
                .username("sayeedakhanum_p")
                .email("sayeedakhanum_p@vnrvjiet.in")
                .passwordHash(SEED_HASH)
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        sayeedaFaculty = facultyRepository.save(Faculty.builder()
                .user(sayeedaUser)
                .department(cseDept)
                .employeeCode("FAC014")
                .firstName("Dr. Sayeedakhanum")
                .lastName("Pathan")
                .designation("Professor")
                .specialization("Database Systems")
                .phone("9876543214")
                .build());

        // 3. Courses
        javaTheoryCourse = courseRepository.save(Course.builder()
                .program(aimlProg)
                .faculty(preetyFaculty)
                .courseCode("25PC1CY201")
                .courseName("Object Oriented Programming Through JAVA")
                .courseType(CourseType.THEORY)
                .credits(new BigDecimal("3.0"))
                .semester(3)
                .build());

        javaLabCourse = courseRepository.save(Course.builder()
                .program(aimlProg)
                .faculty(preetyFaculty)
                .courseCode("25PC2CY201")
                .courseName("Object Oriented Programming Through JAVA Laboratory")
                .courseType(CourseType.LAB)
                .credits(new BigDecimal("1.5"))
                .semester(3)
                .build());

        dbmsCourse = courseRepository.save(Course.builder()
                .program(aimlProg)
                .faculty(sayeedaFaculty)
                .courseCode("25PC1CS201")
                .courseName("Database Management Systems")
                .courseType(CourseType.THEORY)
                .credits(new BigDecimal("3.0"))
                .semester(3)
                .build());

        // 4. Classrooms
        Classroom roomE338 = classroomRepository.save(Classroom.builder()
                .roomNumber("E338")
                .building("Engineering Block")
                .capacity(70)
                .roomType(RoomType.CLASSROOM)
                .build());

        // 5. Timetable (The Single Source of Truth for Faculty Section Scope)
        // Preety Singh -> Java Theory in SEC A
        timetableRepository.save(Timetable.builder()
                .program(aimlProg)
                .course(javaTheoryCourse)
                .faculty(preetyFaculty)
                .classroom(roomE338)
                .academicYear("2024-2025")
                .semester(3)
                .section("A")
                .dayOfWeek("WEDNESDAY")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build());

        // Preety Singh -> Java Lab in SEC A
        timetableRepository.save(Timetable.builder()
                .program(aimlProg)
                .course(javaLabCourse)
                .faculty(preetyFaculty)
                .classroom(roomE338)
                .academicYear("2024-2025")
                .semester(3)
                .section("A")
                .dayOfWeek("THURSDAY")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .build());

        // Sayeedakhanum -> DBMS in SEC B
        timetableRepository.save(Timetable.builder()
                .program(aimlProg)
                .course(dbmsCourse)
                .faculty(sayeedaFaculty)
                .classroom(roomE338)
                .academicYear("2024-2025")
                .semester(3)
                .section("B")
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build());

        // 6. Students across Section A, B, and C
        User stAUser = userRepository.save(User.builder()
                .username("25071A6601")
                .email("25071A6601@vnrvjiet.in")
                .passwordHash(SEED_HASH)
                .role(Role.STUDENT)
                .isActive(true)
                .build());
        studentSecA = studentRepository.save(Student.builder()
                .user(stAUser)
                .program(aimlProg)
                .rollNumber("25071A6601")
                .firstName("Rohith")
                .lastName("Reddy")
                .section("A")
                .admissionYear(2025)
                .currentSemester(3)
                .phone("9991112201")
                .gender("MALE")
                .dateOfBirth(LocalDate.of(2005, 5, 10))
                .build());

        User stBUser = userRepository.save(User.builder()
                .username("25071A6670")
                .email("25071A6670@vnrvjiet.in")
                .passwordHash(SEED_HASH)
                .role(Role.STUDENT)
                .isActive(true)
                .build());
        studentSecB = studentRepository.save(Student.builder()
                .user(stBUser)
                .program(aimlProg)
                .rollNumber("25071A6670")
                .firstName("Sneha")
                .lastName("Patil")
                .section("B")
                .admissionYear(2025)
                .currentSemester(3)
                .phone("9991112270")
                .gender("FEMALE")
                .dateOfBirth(LocalDate.of(2005, 6, 15))
                .build());

        User stCUser = userRepository.save(User.builder()
                .username("25071A66D0")
                .email("25071A66D0@vnrvjiet.in")
                .passwordHash(SEED_HASH)
                .role(Role.STUDENT)
                .isActive(true)
                .build());
        studentSecC = studentRepository.save(Student.builder()
                .user(stCUser)
                .program(aimlProg)
                .rollNumber("25071A66D0")
                .firstName("Vikram")
                .lastName("Sharma")
                .section("C")
                .admissionYear(2025)
                .currentSemester(3)
                .phone("9991112290")
                .gender("MALE")
                .dateOfBirth(LocalDate.of(2005, 7, 20))
                .build());

        // 7. Enrollments
        enrollmentRepository.save(Enrollment.builder()
                .student(studentSecA)
                .course(javaTheoryCourse)
                .academicYear("2024-2025")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .enrollmentDate(LocalDate.now())
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .student(studentSecA)
                .course(javaLabCourse)
                .academicYear("2024-2025")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .enrollmentDate(LocalDate.now())
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .student(studentSecB)
                .course(javaTheoryCourse)
                .academicYear("2024-2025")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .enrollmentDate(LocalDate.now())
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .student(studentSecC)
                .course(javaTheoryCourse)
                .academicYear("2024-2025")
                .semester(3)
                .status(EnrollmentStatus.ACTIVE)
                .enrollmentDate(LocalDate.now())
                .build());

        // 8. Exams
        javaExam1 = examRepository.save(Exam.builder()
                .course(javaTheoryCourse)
                .examName("Sessional Examination - I")
                .examType(ExamType.MID_1)
                .examDate(LocalDate.now().plusDays(10))
                .maxMarks(BigDecimal.valueOf(30))
                .build());

        javaExam2 = examRepository.save(Exam.builder()
                .course(javaTheoryCourse)
                .examName("Sessional Examination - II")
                .examType(ExamType.MID_2)
                .examDate(LocalDate.now().plusDays(20))
                .maxMarks(BigDecimal.valueOf(30))
                .build());

        dbmsExam = examRepository.save(Exam.builder()
                .course(dbmsCourse)
                .examName("Sessional Examination - I")
                .examType(ExamType.MID_1)
                .examDate(LocalDate.now().plusDays(12))
                .maxMarks(BigDecimal.valueOf(30))
                .build());

        // 9. Existing Marks on javaExam1
        markSecA = markRepository.save(Mark.builder()
                .exam(javaExam1)
                .student(studentSecA)
                .marksObtained(new BigDecimal("25.0"))
                .grade("A")
                .enteredBy(preetyFaculty)
                .build());

        markSecB = markRepository.save(Mark.builder()
                .exam(javaExam1)
                .student(studentSecB)
                .marksObtained(new BigDecimal("20.0"))
                .grade("B")
                .enteredBy(preetyFaculty)
                .build());

        // Login Preety Singh
        preetyToken = obtainToken("preety_s", "Password@123");
        // Login Sayeedakhanum
        sayeedaToken = obtainToken("sayeedakhanum_p", "Password@123");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    @Test
    @DisplayName("Preety Singh login returns real profile details (firstName, lastName, fullName, employeeCode)")
    void testPreetyLoginDetails() throws Exception {
        LoginRequest req = new LoginRequest("preety_s", "Password@123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName", is("Preety")))
                .andExpect(jsonPath("$.data.lastName", is("Singh")))
                .andExpect(jsonPath("$.data.fullName", is("Preety Singh")))
                .andExpect(jsonPath("$.data.employeeCode", is("FAC022")));
    }

    @Test
    @DisplayName("Preety Singh: Attendance for Section A -> 200/201 OK")
    void testPreetyAttendanceSectionA_Allowed() throws Exception {
        AttendanceRequest req = new AttendanceRequest();
        req.setCourseId(javaTheoryCourse.getCourseId());
        req.setStudentId(studentSecA.getStudentId()); // SEC A
        req.setAttendanceDate(LocalDate.now());
        req.setStatus(AttendanceStatus.PRESENT);

        mockMvc.perform(post("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("PRESENT")));
    }

    @Test
    @DisplayName("Preety Singh: Attendance for Section B -> 403 Forbidden")
    void testPreetyAttendanceSectionB_Forbidden() throws Exception {
        AttendanceRequest req = new AttendanceRequest();
        req.setCourseId(javaTheoryCourse.getCourseId());
        req.setStudentId(studentSecB.getStudentId()); // SEC B
        req.setAttendanceDate(LocalDate.now());
        req.setStatus(AttendanceStatus.PRESENT);

        mockMvc.perform(post("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Attendance for Section C -> 403 Forbidden")
    void testPreetyAttendanceSectionC_Forbidden() throws Exception {
        AttendanceRequest req = new AttendanceRequest();
        req.setCourseId(javaTheoryCourse.getCourseId());
        req.setStudentId(studentSecC.getStudentId()); // SEC C
        req.setAttendanceDate(LocalDate.now());
        req.setStatus(AttendanceStatus.PRESENT);

        mockMvc.perform(post("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Attendance for unassigned course (DBMS) -> 403 Forbidden")
    void testPreetyAttendanceDBMS_Forbidden() throws Exception {
        AttendanceRequest req = new AttendanceRequest();
        req.setCourseId(dbmsCourse.getCourseId()); // DBMS
        req.setStudentId(studentSecA.getStudentId());
        req.setAttendanceDate(LocalDate.now());
        req.setStatus(AttendanceStatus.PRESENT);

        mockMvc.perform(post("/api/faculty/attendance")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Marks entry for Section A on javaExam2 -> 200/201 OK")
    void testPreetyMarksSectionA_Allowed() throws Exception {
        MarkRequest req = new MarkRequest();
        req.setExamId(javaExam2.getExamId());
        req.setStudentId(studentSecA.getStudentId()); // SEC A
        req.setMarksObtained(new BigDecimal("28.0"));

        mockMvc.perform(post("/api/faculty/marks")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.marksObtained", is(28.0)));
    }

    @Test
    @DisplayName("Preety Singh: Marks entry for Section B -> 403 Forbidden")
    void testPreetyMarksSectionB_Forbidden() throws Exception {
        MarkRequest req = new MarkRequest();
        req.setExamId(javaExam2.getExamId());
        req.setStudentId(studentSecB.getStudentId()); // SEC B
        req.setMarksObtained(new BigDecimal("22.0"));

        mockMvc.perform(post("/api/faculty/marks")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Marks entry for Section C -> 403 Forbidden")
    void testPreetyMarksSectionC_Forbidden() throws Exception {
        MarkRequest req = new MarkRequest();
        req.setExamId(javaExam2.getExamId());
        req.setStudentId(studentSecC.getStudentId()); // SEC C
        req.setMarksObtained(new BigDecimal("22.0"));

        mockMvc.perform(post("/api/faculty/marks")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Marks entry for unassigned course (DBMS) -> 403 Forbidden")
    void testPreetyMarksDBMS_Forbidden() throws Exception {
        MarkRequest req = new MarkRequest();
        req.setExamId(dbmsExam.getExamId()); // DBMS exam
        req.setStudentId(studentSecA.getStudentId());
        req.setMarksObtained(new BigDecimal("26.0"));

        mockMvc.perform(post("/api/faculty/marks")
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Update mark for unauthorized section (Section B) -> 403 Forbidden")
    void testPreetyMarkUpdate_UnauthorizedSection_Forbidden() throws Exception {
        MarkUpdateRequest req = new MarkUpdateRequest();
        req.setMarksObtained(new BigDecimal("24.0"));
        req.setGrade("B+");

        mockMvc.perform(put("/api/faculty/marks/" + markSecB.getMarkId())
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Delete mark for unauthorized section (Section B) -> 403 Forbidden")
    void testPreetyMarkDelete_UnauthorizedSection_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/faculty/marks/" + markSecB.getMarkId())
                        .header("Authorization", "Bearer " + preetyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Preety Singh: Update mark for assigned section (Section A) -> 200 OK")
    void testPreetyMarkUpdate_AssignedSection_Allowed() throws Exception {
        MarkUpdateRequest req = new MarkUpdateRequest();
        req.setMarksObtained(new BigDecimal("29.0"));
        req.setGrade("A+");

        mockMvc.perform(put("/api/faculty/marks/" + markSecA.getMarkId())
                        .header("Authorization", "Bearer " + preetyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marksObtained", is(29.0)));
    }

    @Test
    @DisplayName("Preety Singh: Enrollments endpoint returns ONLY Section A students")
    void testPreetyEnrollments_OnlySectionA() throws Exception {
        mockMvc.perform(get("/api/faculty/enrollments")
                        .header("Authorization", "Bearer " + preetyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2))) // Java Theory Sec A & Java Lab Sec A
                .andExpect(jsonPath("$.data.content[*].section", everyItem(is("A"))))
                .andExpect(jsonPath("$.data.content[*].studentRollNumber", everyItem(is("25071A6601"))));
    }

    @Test
    @DisplayName("Preety Singh: Dashboard metrics reflect only assigned Section A teaching scope")
    void testPreetyDashboard_ScopedScope() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header("Authorization", "Bearer " + preetyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAssignedCourses", is(2)))
                .andExpect(jsonPath("$.data.studentEnrollmentSummary.totalStudents", is(1))) // only Rohith in Sec A
                .andExpect(jsonPath("$.data.assignedCourses", hasSize(2)))
                .andExpect(jsonPath("$.data.assignedCourses[0].section", is("A")))
                .andExpect(jsonPath("$.data.assignedCourses[1].section", is("A")));
    }

    @Test
    @DisplayName("Preety Singh: Timetable endpoint shows only her assigned Section A classes")
    void testPreetyTimetable_ScopedClasses() throws Exception {
        mockMvc.perform(get("/api/faculty/timetable")
                        .header("Authorization", "Bearer " + preetyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].section", everyItem(is("A"))));
    }

    @Test
    @DisplayName("Preety Singh: Exams endpoint shows only Java exams, never DBMS")
    void testPreetyExams_ScopedExams() throws Exception {
        mockMvc.perform(get("/api/faculty/exams")
                        .header("Authorization", "Bearer " + preetyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2))) // javaExam1 and javaExam2
                .andExpect(jsonPath("$.data.content[*].courseCode", everyItem(is("25PC1CY201"))));
    }
}
