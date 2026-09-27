package com.smartcampus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcampus.dto.request.DocumentRequestCreateRequest;
import com.smartcampus.dto.request.DocumentStatusUpdateRequest;
import com.smartcampus.dto.request.DocumentTypeRequest;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.entity.*;
import com.smartcampus.entity.enums.DocumentStatus;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Digital Document & Certificate Service Integration Tests (Phase 9)")
class DocumentServiceIntegrationTest {

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
    private String facultyToken;
    private String student1Token;
    private String student2Token;

    private User adminUser;
    private Student student1;
    private Student student2;
    private DocumentType bonafideType;
    private DocumentType inactiveType;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

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

        // 1. Admin
        adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        // 2. Department & Program
        Department dept = departmentRepository.save(Department.builder()
                .departmentCode("CSE")
                .departmentName("Department of Computer Science & Engineering")
                .build());

        Program program = programRepository.save(Program.builder()
                .department(dept)
                .programCode("BTECH-CSE")
                .programName("B.Tech in Computer Science & Engineering")
                .durationYears(4)
                .build());

        // 3. Faculty
        User facultyUser = userRepository.save(User.builder()
                .username("faculty1")
                .email("faculty1@smartcampus.edu")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        facultyRepository.save(Faculty.builder()
                .user(facultyUser)
                .department(dept)
                .employeeCode("FAC001")
                .firstName("John")
                .lastName("Doe")
                .designation("Professor")
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
                .program(program)
                .rollNumber("23CSE001")
                .firstName("Alice")
                .lastName("Smith")
                .admissionYear(2023)
                .currentSemester(4)
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
                .program(program)
                .rollNumber("23CSE002")
                .firstName("Bob")
                .lastName("Jones")
                .admissionYear(2023)
                .currentSemester(4)
                .section("B")
                .build());

        // 5. Seed document types
        bonafideType = documentTypeRepository.save(DocumentType.builder()
                .documentName("BONAFIDE_CERTIFICATE")
                .description("Bonafide student certificate for bank loans and travel passes")
                .requiresApproval(true)
                .isActive(true)
                .build());

        inactiveType = documentTypeRepository.save(DocumentType.builder()
                .documentName("DISCONTINUED_PASS")
                .description("Legacy document type no longer available")
                .requiresApproval(true)
                .isActive(false)
                .build());

        // 6. Tokens
        adminToken = obtainToken("admin@smartcampus.edu", "Password@123");
        facultyToken = obtainToken("faculty1@smartcampus.edu", "Password@123");
        student1Token = obtainToken("student1@smartcampus.edu", "Password@123");
        student2Token = obtainToken("student2@smartcampus.edu", "Password@123");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        documentRequestHistoryRepository.deleteAll();
        documentRequestRepository.deleteAll();
        documentTypeRepository.deleteAll();
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
    // DOCUMENT TYPES (1–6)
    // ==========================================

    @Test
    @DisplayName("1. Admin can create document type")
    void testAdminCanCreateDocumentType() throws Exception {
        DocumentTypeRequest request = DocumentTypeRequest.builder()
                .documentName("INTERNSHIP_NOC")
                .description("No Objection Certificate for student internship")
                .requiresApproval(true)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/admin/document-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentName").value("INTERNSHIP_NOC"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("2. Admin can list document types")
    void testAdminCanListDocumentTypes() throws Exception {
        mockMvc.perform(get("/api/admin/document-types")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("3. Admin can update document type")
    void testAdminCanUpdateDocumentType() throws Exception {
        DocumentTypeRequest updateRequest = DocumentTypeRequest.builder()
                .documentName("BONAFIDE_CERTIFICATE_V2")
                .description("Updated description for bonafide certificate")
                .requiresApproval(true)
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/admin/document-types/" + bonafideType.getDocumentTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentName").value("BONAFIDE_CERTIFICATE_V2"))
                .andExpect(jsonPath("$.data.description").value("Updated description for bonafide certificate"));
    }

    @Test
    @DisplayName("4. Duplicate document type rejected with 409 Conflict")
    void testDuplicateDocumentTypeRejected() throws Exception {
        DocumentTypeRequest request = DocumentTypeRequest.builder()
                .documentName("BONAFIDE_CERTIFICATE")
                .description("Duplicate name test")
                .requiresApproval(true)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/admin/document-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("5. Student sees active document types")
    void testStudentSeesActiveDocumentTypes() throws Exception {
        mockMvc.perform(get("/api/student/document-types")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].documentName", hasItem("BONAFIDE_CERTIFICATE")));
    }

    @Test
    @DisplayName("6. Inactive document type not shown to students")
    void testInactiveDocumentTypeNotShownToStudents() throws Exception {
        mockMvc.perform(get("/api/student/document-types")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].documentName", not(hasItem("DISCONTINUED_PASS"))));
    }

    // ==========================================
    // STUDENT REQUEST (7–13)
    // ==========================================

    @Test
    @DisplayName("7. Student can create document request")
    void testStudentCanCreateDocumentRequest() throws Exception {
        DocumentRequestCreateRequest request = DocumentRequestCreateRequest.builder()
                .documentTypeId(bonafideType.getDocumentTypeId())
                .purpose("Applying for state scholarship assistance")
                .additionalDetails("Required by department before Friday")
                .build();

        mockMvc.perform(post("/api/student/document-requests")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.purpose").value("Applying for state scholarship assistance"))
                .andExpect(jsonPath("$.data.studentRollNumber").value("23CSE001"));
    }

    @Test
    @DisplayName("8. Request starts as SUBMITTED")
    void testRequestStartsAsSubmitted() throws Exception {
        DocumentRequestCreateRequest request = DocumentRequestCreateRequest.builder()
                .documentTypeId(bonafideType.getDocumentTypeId())
                .purpose("Bus pass concession")
                .build();

        mockMvc.perform(post("/api/student/document-requests")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("9. Request number generated")
    void testRequestNumberGenerated() throws Exception {
        DocumentRequestCreateRequest request = DocumentRequestCreateRequest.builder()
                .documentTypeId(bonafideType.getDocumentTypeId())
                .purpose("Education loan application")
                .build();

        mockMvc.perform(post("/api/student/document-requests")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.requestNumber", startsWith("DOC-")));
    }

    @Test
    @DisplayName("10. History entry created on request creation")
    void testHistoryEntryCreatedOnCreation() throws Exception {
        DocumentRequestCreateRequest request = DocumentRequestCreateRequest.builder()
                .documentTypeId(bonafideType.getDocumentTypeId())
                .purpose("Passport verification")
                .build();

        MvcResult result = mockMvc.perform(post("/api/student/document-requests")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        long requestId = root.path("data").path("requestId").asLong();

        mockMvc.perform(get("/api/student/document-requests/" + requestId + "/history")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].newStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.data[0].oldStatus").isEmpty())
                .andExpect(jsonPath("$.data[0].remarks").value("Document request submitted"));
    }

    @Test
    @DisplayName("11. Student can list own requests")
    void testStudentCanListOwnRequests() throws Exception {
        createSampleRequestForStudent1("Purpose 1");
        createSampleRequestForStudent1("Purpose 2");

        mockMvc.perform(get("/api/student/document-requests")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    @DisplayName("12. Student can view own request")
    void testStudentCanViewOwnRequest() throws Exception {
        long reqId = createSampleRequestForStudent1("Specific purpose");

        mockMvc.perform(get("/api/student/document-requests/" + reqId)
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestId").value(reqId))
                .andExpect(jsonPath("$.data.purpose").value("Specific purpose"));
    }

    @Test
    @DisplayName("13. Student can view own history")
    void testStudentCanViewOwnHistory() throws Exception {
        long reqId = createSampleRequestForStudent1("History check");

        mockMvc.perform(get("/api/student/document-requests/" + reqId + "/history")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // ==========================================
    // OWNERSHIP (14–17)
    // ==========================================

    @Test
    @DisplayName("14. Student cannot view another student's request (403)")
    void testStudentCannotViewAnotherStudentRequest() throws Exception {
        long student1ReqId = createSampleRequestForStudent1("Student 1 secret purpose");

        mockMvc.perform(get("/api/student/document-requests/" + student1ReqId)
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. Student cannot view another student's history (403)")
    void testStudentCannotViewAnotherStudentHistory() throws Exception {
        long student1ReqId = createSampleRequestForStudent1("Student 1 history check");

        mockMvc.perform(get("/api/student/document-requests/" + student1ReqId + "/history")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("16. Student cannot download another student's document (403)")
    void testStudentCannotDownloadAnotherStudentDocument() throws Exception {
        long student1ReqId = createSampleRequestForStudent1("To be issued");
        transitionToIssued(student1ReqId);

        mockMvc.perform(get("/api/student/document-requests/" + student1ReqId + "/download")
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("17. Student cannot approve/reject/issue (403)")
    void testStudentCannotChangeRequestStatus() throws Exception {
        long student1ReqId = createSampleRequestForStudent1("Attempt self approve");
        DocumentStatusUpdateRequest update = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.APPROVED)
                .remarks("Self approving")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + student1ReqId + "/status")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // STATUS TRANSITIONS (18–23)
    // ==========================================

    @Test
    @DisplayName("18. Admin can move SUBMITTED → UNDER_REVIEW")
    void testAdminCanMoveSubmittedToUnderReview() throws Exception {
        long reqId = createSampleRequestForStudent1("Review transition test");

        DocumentStatusUpdateRequest update = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.UNDER_REVIEW)
                .remarks("Review started by admin")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.data.reviewedByUsername").value("admin"));
    }

    @Test
    @DisplayName("19. Admin can move UNDER_REVIEW → APPROVED")
    void testAdminCanMoveUnderReviewToApproved() throws Exception {
        long reqId = createSampleRequestForStudent1("Approval test");
        transitionToStatus(reqId, DocumentStatus.UNDER_REVIEW, "Verification OK");

        DocumentStatusUpdateRequest update = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.APPROVED)
                .remarks("Approved for certificate generation")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("20. Admin can move APPROVED → ISSUED")
    void testAdminCanMoveApprovedToIssued() throws Exception {
        long reqId = createSampleRequestForStudent1("Issue test");
        transitionToStatus(reqId, DocumentStatus.UNDER_REVIEW, "Verification OK");
        transitionToStatus(reqId, DocumentStatus.APPROVED, "Approved");

        DocumentStatusUpdateRequest update = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.ISSUED)
                .remarks("Digital certificate issued")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ISSUED"))
                .andExpect(jsonPath("$.data.verificationCode", notNullValue()))
                .andExpect(jsonPath("$.data.documentPath", notNullValue()));
    }

    @Test
    @DisplayName("21. Admin can reject SUBMITTED")
    void testAdminCanRejectSubmitted() throws Exception {
        long reqId = createSampleRequestForStudent1("Reject submitted test");

        DocumentStatusUpdateRequest update = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.REJECTED)
                .remarks("Incomplete student documentation provided")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("22. Admin can reject UNDER_REVIEW")
    void testAdminCanRejectUnderReview() throws Exception {
        long reqId = createSampleRequestForStudent1("Reject under review test");
        transitionToStatus(reqId, DocumentStatus.UNDER_REVIEW, "Started review");

        DocumentStatusUpdateRequest update = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.REJECTED)
                .remarks("Fee dues pending")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("23. Invalid status transition rejected (400 Bad Request)")
    void testInvalidStatusTransitionRejected() throws Exception {
        long reqId = createSampleRequestForStudent1("Invalid jump test");

        // Attempt SUBMITTED -> APPROVED (illegal jump)
        DocumentStatusUpdateRequest jumpToApproved = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.APPROVED)
                .remarks("Illegal jump")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jumpToApproved)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Attempt SUBMITTED -> ISSUED (illegal jump)
        DocumentStatusUpdateRequest jumpToIssued = DocumentStatusUpdateRequest.builder()
                .status(DocumentStatus.ISSUED)
                .remarks("Illegal jump directly to issued")
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + reqId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jumpToIssued)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // AUDIT (24–25)
    // ==========================================

    @Test
    @DisplayName("24. Every status change creates history")
    void testEveryStatusChangeCreatesHistory() throws Exception {
        long reqId = createSampleRequestForStudent1("Full lifecycle audit test");
        transitionToStatus(reqId, DocumentStatus.UNDER_REVIEW, "Review step");
        transitionToStatus(reqId, DocumentStatus.APPROVED, "Approve step");
        transitionToStatus(reqId, DocumentStatus.ISSUED, "Issue step");

        mockMvc.perform(get("/api/admin/document-requests/" + reqId + "/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4))) // SUBMITTED, UNDER_REVIEW, APPROVED, ISSUED
                .andExpect(jsonPath("$.data[0].newStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.data[1].newStatus").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.data[2].newStatus").value("APPROVED"))
                .andExpect(jsonPath("$.data[3].newStatus").value("ISSUED"));
    }

    @Test
    @DisplayName("25. Reviewer identity comes from authenticated user")
    void testReviewerIdentityComesFromAuthenticatedUser() throws Exception {
        long reqId = createSampleRequestForStudent1("Reviewer audit test");
        transitionToStatus(reqId, DocumentStatus.UNDER_REVIEW, "Review by admin");

        mockMvc.perform(get("/api/admin/document-requests/" + reqId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewedByUsername").value("admin"))
                .andExpect(jsonPath("$.data.reviewedByUserId").value(adminUser.getUserId()));
    }

    // ==========================================
    // CERTIFICATE & DOWNLOAD (26–31)
    // ==========================================

    @Test
    @DisplayName("26. ISSUED request gets verification code")
    void testIssuedRequestGetsVerificationCode() throws Exception {
        long reqId = createSampleRequestForStudent1("Verification code check");
        transitionToIssued(reqId);

        mockMvc.perform(get("/api/student/document-requests/" + reqId)
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationCode", startsWith("SCMS-")));
    }

    @Test
    @DisplayName("27. ISSUED request gets document path")
    void testIssuedRequestGetsDocumentPath() throws Exception {
        long reqId = createSampleRequestForStudent1("Path check");
        transitionToIssued(reqId);

        mockMvc.perform(get("/api/student/document-requests/" + reqId)
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentPath", endsWith(".pdf")));
    }

    @Test
    @DisplayName("28. Generated document exists on disk")
    void testGeneratedDocumentExistsOnDisk() throws Exception {
        long reqId = createSampleRequestForStudent1("Disk file check");
        transitionToIssued(reqId);

        DocumentRequest req = documentRequestRepository.findById(reqId).orElseThrow();
        assertNotNull(req.getDocumentPath());

        Path path = Paths.get(req.getDocumentPath());
        assertTrue(Files.exists(path), "Generated PDF document should exist at " + path);
        assertTrue(Files.size(path) > 0, "Generated PDF document should not be empty");
    }

    @Test
    @DisplayName("29. Student can download own issued document")
    void testStudentCanDownloadOwnIssuedDocument() throws Exception {
        long reqId = createSampleRequestForStudent1("Student download check");
        transitionToIssued(reqId);

        mockMvc.perform(get("/api/student/document-requests/" + reqId + "/download")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assertNotNull(contentType);
                    assertTrue(contentType.contains("application/pdf"));
                });
    }

    @Test
    @DisplayName("30. Admin can download issued document")
    void testAdminCanDownloadIssuedDocument() throws Exception {
        long reqId = createSampleRequestForStudent1("Admin download check");
        transitionToIssued(reqId);

        mockMvc.perform(get("/api/admin/document-requests/" + reqId + "/download")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assertNotNull(contentType);
                    assertTrue(contentType.contains("application/pdf"));
                });
    }

    @Test
    @DisplayName("31. Non-issued document cannot be downloaded (400 Bad Request)")
    void testNonIssuedDocumentCannotBeDownloaded() throws Exception {
        long reqId = createSampleRequestForStudent1("Premature download check");

        mockMvc.perform(get("/api/student/document-requests/" + reqId + "/download")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // VERIFICATION (32–34)
    // ==========================================

    @Test
    @DisplayName("32. Valid verification code returns document information")
    void testValidVerificationCodeReturnsInfo() throws Exception {
        long reqId = createSampleRequestForStudent1("Public verification check");
        transitionToIssued(reqId);

        DocumentRequest req = documentRequestRepository.findById(reqId).orElseThrow();
        String code = req.getVerificationCode();

        mockMvc.perform(get("/api/public/documents/verify/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.requestNumber").value(req.getRequestNumber()))
                .andExpect(jsonPath("$.data.studentName", containsString("Alice")))
                .andExpect(jsonPath("$.data.status").value("ISSUED"));
    }

    @Test
    @DisplayName("33. Invalid verification code returns appropriate error (404 Not Found)")
    void testInvalidVerificationCodeReturnsError() throws Exception {
        mockMvc.perform(get("/api/public/documents/verify/NON-EXISTENT-CODE-12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("34. Rejected/non-issued document cannot verify successfully")
    void testRejectedDocumentCannotVerifySuccessfully() throws Exception {
        long reqId = createSampleRequestForStudent1("Reject check");
        transitionToStatus(reqId, DocumentStatus.REJECTED, "Application rejected");

        DocumentRequest req = documentRequestRepository.findById(reqId).orElseThrow();
        // Even if a non-issued document had a verification code, public verify must reject it
        req.setVerificationCode("TEST-REJECTED-CODE-999");
        documentRequestRepository.save(req);

        mockMvc.perform(get("/api/public/documents/verify/TEST-REJECTED-CODE-999"))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // SECURITY & ACCESS CONTROL (35–38)
    // ==========================================

    @Test
    @DisplayName("35. Unauthenticated request returns 401")
    void testUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/document-types"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/student/document-requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("36. Student accessing admin document endpoint returns 403")
    void testStudentAccessingAdminEndpointReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/document-requests")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("37. Faculty cannot perform admin-only document management (403)")
    void testFacultyCannotManageDocumentTypesOrRequests() throws Exception {
        mockMvc.perform(get("/api/admin/document-types")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/document-requests")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("38. Admin can access administrative request endpoints")
    void testAdminCanAccessAdminRequestEndpoints() throws Exception {
        createSampleRequestForStudent1("Admin listing sample");

        mockMvc.perform(get("/api/admin/document-requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", not(empty())));
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private long createSampleRequestForStudent1(String purpose) throws Exception {
        DocumentRequestCreateRequest request = DocumentRequestCreateRequest.builder()
                .documentTypeId(bonafideType.getDocumentTypeId())
                .purpose(purpose)
                .additionalDetails("Sample additional remarks")
                .build();

        MvcResult result = mockMvc.perform(post("/api/student/document-requests")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("requestId").asLong();
    }

    private void transitionToStatus(long requestId, DocumentStatus targetStatus, String remarks) throws Exception {
        DocumentStatusUpdateRequest update = DocumentStatusUpdateRequest.builder()
                .status(targetStatus)
                .remarks(remarks)
                .build();

        mockMvc.perform(put("/api/admin/document-requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }

    private void transitionToIssued(long requestId) throws Exception {
        transitionToStatus(requestId, DocumentStatus.UNDER_REVIEW, "Moved to review");
        transitionToStatus(requestId, DocumentStatus.APPROVED, "Moved to approved");
        transitionToStatus(requestId, DocumentStatus.ISSUED, "Certificate issued");
    }
}
