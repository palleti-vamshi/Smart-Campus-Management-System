package com.smartcampus.controller;

import com.smartcampus.dto.dashboard.AdminDashboardResponse;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for department-level Admin dashboard operations.
 * Read-only aggregation of department summary, student distributions,
 * attendance, academic performance, enrollments, timetables, notices, and documents.
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard retrieved successfully", response));
    }
}
