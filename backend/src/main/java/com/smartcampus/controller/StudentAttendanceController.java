package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.AttendanceSummaryResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.AttendanceStatus;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.AttendanceService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Student Attendance operations.
 * STUDENT access only. Read-only access to own attendance and attendance summary.
 */
@RestController
@RequestMapping("/api/student/attendance")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getMyAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status,
            @PageableDefault(size = 20, sort = "attendanceDate") Pageable pageable) {
        PageResponse<AttendanceResponse> response = attendanceService.getAttendanceForStudent(
                userDetails.getUserId(), courseId, startDate, endDate, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> getMyAttendanceSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AttendanceSummaryResponse> summary = attendanceService.getStudentAttendanceSummary(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Attendance summary retrieved successfully", summary));
    }
}
