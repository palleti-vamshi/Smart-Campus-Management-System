package com.smartcampus.security;

import com.smartcampus.entity.User;
import com.smartcampus.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    private JwtTokenProvider tokenProvider;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION_MS);

        User user = User.builder()
                .userId(1L)
                .username("admin")
                .email("admin@smartcampus.edu")
                .passwordHash("$2a$10$dummyHash")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        userDetails = CustomUserDetails.build(user);
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void testGenerateToken() {
        String token = tokenProvider.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT should consist of 3 base64url segments");
    }

    @Test
    @DisplayName("Should extract correct claims from JWT token")
    void testExtractClaims() {
        String token = tokenProvider.generateToken(userDetails);

        assertEquals("admin", tokenProvider.getUsernameFromToken(token));
        assertEquals(1L, tokenProvider.getUserIdFromToken(token));
        assertEquals("ADMIN", tokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Should validate valid JWT token")
    void testValidateValidToken() {
        String token = tokenProvider.generateToken(userDetails);

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should reject tampered or malformed JWT token")
    void testValidateMalformedToken() {
        String token = tokenProvider.generateToken(userDetails);
        String tamperedToken = token + "xyz";

        assertFalse(tokenProvider.validateToken(tamperedToken));
        assertFalse(tokenProvider.validateToken("not.a.valid.jwt"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Should reject expired JWT token")
    void testValidateExpiredToken() {
        // Expired immediately (-1000ms)
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, -1000);
        String expiredToken = expiredProvider.generateToken(userDetails);

        assertFalse(expiredProvider.validateToken(expiredToken));
    }
}
