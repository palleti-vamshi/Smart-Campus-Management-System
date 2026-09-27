package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.FacultyResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.FacultyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Faculty self-service operations.
 * Restricted to authenticated FACULTY users.
 */
@RestController
@RequestMapping("/api/faculty")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyProfileController {

    private final FacultyService facultyService;

    public FacultyProfileController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    /**
     * Retrieves the authenticated faculty member's own profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<FacultyResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        FacultyResponse profile = facultyService.getFacultyByUserId(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Faculty profile retrieved successfully", profile));
    }
}
