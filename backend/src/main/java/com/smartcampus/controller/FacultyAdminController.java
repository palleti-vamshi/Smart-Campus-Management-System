package com.smartcampus.controller;

import com.smartcampus.dto.request.FacultyRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.FacultyResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Faculty master data administration.
 * Administrative access only.
 */
@RestController
@RequestMapping("/api/admin/faculty")
@PreAuthorize("hasRole('ADMIN')")
public class FacultyAdminController {

    private final FacultyService facultyService;

    public FacultyAdminController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FacultyResponse>>> getFaculty(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<FacultyResponse> response = facultyService.getFaculty(departmentId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Faculty members retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFacultyById(@PathVariable Long id) {
        FacultyResponse faculty = facultyService.getFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty member retrieved successfully", faculty));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FacultyResponse>> createFaculty(
            @Valid @RequestBody FacultyRequest request) {
        FacultyResponse created = facultyService.createFaculty(request);
        return new ResponseEntity<>(ApiResponse.success("Faculty member created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyResponse>> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyRequest request) {
        FacultyResponse updated = facultyService.updateFaculty(id, request);
        return ResponseEntity.ok(ApiResponse.success("Faculty member updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty member deleted successfully"));
    }
}
