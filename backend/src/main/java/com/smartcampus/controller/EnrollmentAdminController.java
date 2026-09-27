package com.smartcampus.controller;

import com.smartcampus.dto.request.EnrollmentRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.EnrollmentResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.EnrollmentStatus;
import com.smartcampus.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Enrollment administration.
 * ADMIN access only.
 */
@RestController
@RequestMapping("/api/admin/enrollments")
@PreAuthorize("hasRole('ADMIN')")
public class EnrollmentAdminController {

    private final EnrollmentService enrollmentService;

    public EnrollmentAdminController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getEnrollments(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20, sort = "enrollmentId") Pageable pageable) {
        PageResponse<EnrollmentResponse> response = enrollmentService.getEnrollmentsForAdmin(
                studentId, courseId, academicYear, semester, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Enrollments retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(@PathVariable Long id) {
        EnrollmentResponse response = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> createEnrollment(
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse created = enrollmentService.createEnrollment(request);
        return new ResponseEntity<>(ApiResponse.success("Enrollment created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse updated = enrollmentService.updateEnrollment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Enrollment updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment deleted successfully"));
    }
}
