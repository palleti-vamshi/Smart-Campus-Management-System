package com.smartcampus.controller;

import com.smartcampus.dto.request.TimetableRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.dto.response.TimetableResponse;
import com.smartcampus.service.TimetableService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Timetable administration.
 * ADMIN access only.
 */
@RestController
@RequestMapping("/api/admin/timetable")
@PreAuthorize("hasRole('ADMIN')")
public class TimetableAdminController {

    private final TimetableService timetableService;

    public TimetableAdminController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TimetableResponse>>> getTimetable(
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) String dayOfWeek,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String academicYear,
            @PageableDefault(size = 20, sort = "timetableId") Pageable pageable) {
        PageResponse<TimetableResponse> response = timetableService.getTimetableForAdmin(
                programId, section, courseId, facultyId, classroomId, dayOfWeek, semester, academicYear, pageable);
        return ResponseEntity.ok(ApiResponse.success("Timetable retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimetableResponse>> getTimetableById(@PathVariable Long id) {
        TimetableResponse response = timetableService.getTimetableById(id);
        return ResponseEntity.ok(ApiResponse.success("Timetable entry retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimetableResponse>> createTimetable(
            @Valid @RequestBody TimetableRequest request) {
        TimetableResponse created = timetableService.createTimetable(request);
        return new ResponseEntity<>(ApiResponse.success("Timetable entry created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimetableResponse>> updateTimetable(
            @PathVariable Long id,
            @Valid @RequestBody TimetableRequest request) {
        TimetableResponse updated = timetableService.updateTimetable(id, request);
        return ResponseEntity.ok(ApiResponse.success("Timetable entry updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimetable(@PathVariable Long id) {
        timetableService.deleteTimetable(id);
        return ResponseEntity.ok(ApiResponse.success("Timetable entry deleted successfully"));
    }
}
