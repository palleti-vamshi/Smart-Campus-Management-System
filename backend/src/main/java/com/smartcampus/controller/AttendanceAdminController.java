package com.smartcampus.controller;

import com.smartcampus.dto.request.AttendanceRequest;
import com.smartcampus.dto.request.AttendanceUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.AttendanceStatus;
import com.smartcampus.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for Attendance administration.
 * ADMIN access only.
 */
@RestController
@RequestMapping("/api/admin/attendance")
@PreAuthorize("hasRole('ADMIN')")
public class AttendanceAdminController {

    private final AttendanceService attendanceService;

    public AttendanceAdminController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getAttendance(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status,
            @PageableDefault(size = 20, sort = "attendanceId") Pageable pageable) {
        PageResponse<AttendanceResponse> response = attendanceService.getAttendanceForAdmin(
                courseId, studentId, facultyId, attendanceDate, startDate, endDate, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(@PathVariable Long id) {
        AttendanceResponse response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance record retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> recordAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse created = attendanceService.recordAttendanceByAdmin(request);
        return new ResponseEntity<>(ApiResponse.success("Attendance recorded successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        AttendanceResponse updated = attendanceService.updateAttendanceByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendanceByAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance deleted successfully"));
    }
}
