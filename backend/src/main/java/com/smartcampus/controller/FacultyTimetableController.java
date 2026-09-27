package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.dto.response.TimetableResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.TimetableService;
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
 * Controller for Faculty Timetable operations.
 * FACULTY access only. Read-only view of own timetable.
 */
@RestController
@RequestMapping("/api/faculty/timetable")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyTimetableController {

    private final TimetableService timetableService;

    public FacultyTimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TimetableResponse>>> getMyTimetable(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String dayOfWeek,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String academicYear,
            @PageableDefault(size = 20, sort = "timetableId") Pageable pageable) {
        PageResponse<TimetableResponse> response = timetableService.getTimetableForFaculty(
                userDetails.getUserId(), dayOfWeek, semester, academicYear, pageable);
        return ResponseEntity.ok(ApiResponse.success("Faculty timetable retrieved successfully", response));
    }
}
