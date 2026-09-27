package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller providing test/verification endpoints for role-based authorization.
 * Used to verify access control for ADMIN, FACULTY, and STUDENT roles.
 */
@RestController
@RequestMapping("/api")
public class SecurityTestController {

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminOnlyEndpoint(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Admin access granted", Map.of(
                "message", "Admin endpoint accessed successfully",
                "username", userDetails.getUsername(),
                "role", userDetails.getRole().name()
        )));
    }

    @GetMapping("/faculty/test")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> facultyOnlyEndpoint(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Faculty access granted", Map.of(
                "message", "Faculty endpoint accessed successfully",
                "username", userDetails.getUsername(),
                "role", userDetails.getRole().name()
        )));
    }

    @GetMapping("/student/test")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> studentOnlyEndpoint(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Student access granted", Map.of(
                "message", "Student endpoint accessed successfully",
                "username", userDetails.getUsername(),
                "role", userDetails.getRole().name()
        )));
    }
}
