package com.smartcampus.controller;

import com.smartcampus.dto.dashboard.StudentDashboardResponse;
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
 * REST controller for Student dashboard operations.
 * Read-only summary restricted to the authenticated student's profile, enrollments,
 * attendance, marks, timetable, notices, and document requests.
 */
@RestController
@RequestMapping("/api/student/dashboard")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StudentDashboardResponse response = dashboardService.getStudentDashboard(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Student dashboard retrieved successfully", response));
    }
}
