package com.smartcampus.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.entity.User;
import com.smartcampus.entity.enums.Role;
import com.smartcampus.repository.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Authentication and Role Security Integration Tests")
class AuthenticationSecurityIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String SEED_HASH = "$2a$10$/vqkFH.7kR5J9g1ym.jGrefcoQO0YVp7HxwX38fg7YMC.2V1O3aeK";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();

        // Seed ADMIN user with exact Phase 1 BCrypt hash
        userRepository.save(User.builder()
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash(SEED_HASH)
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        // Seed FACULTY user with exact Phase 1 BCrypt hash
        userRepository.save(User.builder()
                .username("sagar_y")
                .email("sagar_y@vnrvjiet.in")
                .passwordHash(SEED_HASH)
                .role(Role.FACULTY)
                .isActive(true)
                .build());

        // Seed STUDENT user with exact Phase 1 BCrypt hash
        userRepository.save(User.builder()
                .username("25071A6601")
                .email("25071A6601@vnrvjiet.in")
                .passwordHash(SEED_HASH)
                .role(Role.STUDENT)
                .isActive(true)
                .build());

        // Seed INACTIVE user
        userRepository.save(User.builder()
                .username("inactive_student")
                .email("inactive@smartcampus.edu")
                .passwordHash(SEED_HASH)
                .role(Role.STUDENT)
                .isActive(false)
                .build());
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
    // 1. Valid Admin Login → 200
    // ==========================================
    @Test
    @DisplayName("1. Valid admin login should return 200 and JWT access token")
    void testValidAdminLogin() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonNode -> assertNotNull(jsonNode.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.username", is("admin")))
                .andExpect(jsonPath("$.data.email", is("admin@smartcampus.edu")))
                .andExpect(jsonPath("$.data.role", is("ADMIN")))
                .andExpect(jsonPath("$.data.userId", notNullValue()));
    }

    // ==========================================
    // 2. Valid Faculty Login → 200
    // ==========================================
    @Test
    @DisplayName("2. Valid faculty login should return 200 and JWT access token")
    void testValidFacultyLogin() throws Exception {
        LoginRequest request = new LoginRequest("sagar_y", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.username", is("sagar_y")))
                .andExpect(jsonPath("$.data.email", is("sagar_y@vnrvjiet.in")))
                .andExpect(jsonPath("$.data.role", is("FACULTY")));
    }

    // ==========================================
    // 3. Valid Student Login → 200
    // ==========================================
    @Test
    @DisplayName("3. Valid student login should return 200 and JWT access token")
    void testValidStudentLogin() throws Exception {
        LoginRequest request = new LoginRequest("25071A6601", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.username", is("25071A6601")))
                .andExpect(jsonPath("$.data.email", is("25071A6601@vnrvjiet.in")))
                .andExpect(jsonPath("$.data.role", is("STUDENT")));
    }

    // ==========================================
    // 4. Invalid Password → 401
    // ==========================================
    @Test
    @DisplayName("4. Invalid password should return 401 Unauthorized")
    void testInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("admin", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid username or password")));
    }

    @Test
    @DisplayName("4b. Non-existent username should return 401 Unauthorized")
    void testNonExistentUsername() throws Exception {
        LoginRequest request = new LoginRequest("non_existent_user", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    // ==========================================
    // 5. Missing JWT → 401
    // ==========================================
    @Test
    @DisplayName("5. Accessing protected endpoint without JWT should return 401 Unauthorized")
    void testMissingJwt() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Full authentication is required to access this resource")));
    }

    // ==========================================
    // 6. Invalid JWT → 401
    // ==========================================
    @Test
    @DisplayName("6. Accessing protected endpoint with invalid JWT should return 401 Unauthorized")
    void testInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer invalid.jwt.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    // ==========================================
    // 7. Insufficient Role → 403
    // ==========================================
    @Test
    @DisplayName("7. Student token attempting to access admin endpoint should return 403 Forbidden")
    void testInsufficientRoleStudentToAdmin() throws Exception {
        String studentToken = obtainAccessToken("25071A6601", "Password@123");

        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("denied")));
    }

    @Test
    @DisplayName("7b. Faculty token attempting to access admin endpoint should return 403 Forbidden")
    void testInsufficientRoleFacultyToAdmin() throws Exception {
        String facultyToken = obtainAccessToken("sagar_y", "Password@123");

        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("7c. Student token attempting to access faculty endpoint should return 403 Forbidden")
    void testInsufficientRoleStudentToFaculty() throws Exception {
        String studentToken = obtainAccessToken("25071A6601", "Password@123");

        mockMvc.perform(get("/api/faculty/test")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    // ==========================================
    // 8. Protected Endpoint with Correct Role → 200
    // ==========================================
    @Test
    @DisplayName("8a. Admin token accessing admin endpoint should return 200 OK")
    void testAdminAccessToAdminEndpoint() throws Exception {
        String adminToken = obtainAccessToken("admin", "Password@123");

        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("admin")))
                .andExpect(jsonPath("$.data.role", is("ADMIN")));
    }

    @Test
    @DisplayName("8b. Faculty token accessing faculty endpoint should return 200 OK")
    void testFacultyAccessToFacultyEndpoint() throws Exception {
        String facultyToken = obtainAccessToken("sagar_y", "Password@123");

        mockMvc.perform(get("/api/faculty/test")
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("sagar_y")))
                .andExpect(jsonPath("$.data.role", is("FACULTY")));
    }

    @Test
    @DisplayName("8c. Student token accessing student endpoint should return 200 OK")
    void testStudentAccessToStudentEndpoint() throws Exception {
        String studentToken = obtainAccessToken("25071A6601", "Password@123");

        mockMvc.perform(get("/api/student/test")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("25071A6601")))
                .andExpect(jsonPath("$.data.role", is("STUDENT")));
    }

    // ==========================================
    // 9. Input Validation & Edge Cases
    // ==========================================
    @Test
    @DisplayName("9. Blank username or password should return 400 Bad Request")
    void testBlankLoginFields() throws Exception {
        LoginRequest blankUsername = new LoginRequest("", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankUsername)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        LoginRequest blankPassword = new LoginRequest("admin", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("10. Inactive user should return 401 Unauthorized")
    void testInactiveUserLogin() throws Exception {
        LoginRequest request = new LoginRequest("inactive_student", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }
}
