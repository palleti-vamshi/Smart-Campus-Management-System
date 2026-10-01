package com.smartcampus.controller;

import com.smartcampus.dto.request.AttendanceRequest;
import com.smartcampus.dto.request.AttendanceUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.AttendanceStatus;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for Faculty Attendance operations.
 * FACULTY access only. Restricted strictly to courses taught by the authenticated faculty member.
 */
@RestController
@RequestMapping("/api/faculty/attendance")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyAttendanceController {

    private final AttendanceService attendanceService;

    public FacultyAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> recordAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.recordAttendanceByFaculty(userDetails.getUserId(), request);
        return new ResponseEntity<>(ApiResponse.success("Attendance marked successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<java.util.List<AttendanceResponse>>> recordBatchAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody com.smartcampus.dto.request.BatchAttendanceRequest request) {
        java.util.List<AttendanceResponse> response = attendanceService.recordBatchAttendanceByFaculty(userDetails.getUserId(), request);
        return new ResponseEntity<>(ApiResponse.success("Batch attendance saved successfully", response), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status,
            @PageableDefault(size = 20, sort = "attendanceId") Pageable pageable) {
        PageResponse<AttendanceResponse> response = attendanceService.getAttendanceForFaculty(
                userDetails.getUserId(), courseId, studentId, section, attendanceDate, startDate, endDate, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        AttendanceResponse response = attendanceService.updateAttendanceByFaculty(userDetails.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        attendanceService.deleteAttendanceByFaculty(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Attendance deleted successfully"));
    }
}
