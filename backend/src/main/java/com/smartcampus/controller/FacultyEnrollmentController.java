package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.EnrollmentResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.EnrollmentStatus;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.EnrollmentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Faculty enrollment views.
 * FACULTY access only. Read-only access to enrollments for courses taught by the authenticated faculty member.
 */
@RestController
@RequestMapping("/api/faculty/enrollments")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyEnrollmentController {

    private final EnrollmentService enrollmentService;

    public FacultyEnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getAssignedCourseEnrollments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20, sort = "enrollmentId") Pageable pageable) {
        PageResponse<EnrollmentResponse> response = enrollmentService.getEnrollmentsForFaculty(
                userDetails.getUserId(), courseId, academicYear, semester, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Course enrollments retrieved successfully", response));
    }
}
