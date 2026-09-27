package com.smartcampus.controller;

import com.smartcampus.dto.dashboard.FacultyDashboardResponse;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Faculty dashboard operations.
 * Read-only summary restricted to courses assigned to the authenticated faculty member.
 */
@RestController
@RequestMapping("/api/faculty/dashboard")
@PreAuthorize("hasRole('FACULTY')")
@RequiredArgsConstructor
public class FacultyDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<FacultyDashboardResponse>> getFacultyDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        FacultyDashboardResponse response = dashboardService.getFacultyDashboard(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Faculty dashboard retrieved successfully", response));
    }
}
