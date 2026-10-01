package com.smartcampus.controller;

import com.smartcampus.dto.request.MarkRequest;
import com.smartcampus.dto.request.MarkUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.MarkResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.MarkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Faculty Mark operations.
 * FACULTY access only. Restricted to exams belonging to courses taught by the authenticated faculty member.
 */
@RestController
@RequestMapping("/api/faculty/marks")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyMarkController {

    private final MarkService markService;

    public FacultyMarkController(MarkService markService) {
        this.markService = markService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MarkResponse>>> getMarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String section,
            @PageableDefault(size = 20, sort = "markId") Pageable pageable) {
        PageResponse<MarkResponse> response = markService.getMarksForFaculty(
                userDetails.getUserId(), examId, studentId, courseId, section, pageable);
        return ResponseEntity.ok(ApiResponse.success("Marks retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MarkResponse>> createMark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MarkRequest request) {
        MarkResponse created = markService.createMarkByFaculty(userDetails.getUserId(), request);
        return new ResponseEntity<>(ApiResponse.success("Mark recorded successfully", created), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<java.util.List<MarkResponse>>> createBatchMarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody com.smartcampus.dto.request.BatchMarkRequest request) {
        java.util.List<MarkResponse> responses = markService.createOrUpdateBatchMarksByFaculty(userDetails.getUserId(), request);
        return new ResponseEntity<>(ApiResponse.success("Batch marks recorded successfully", responses), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MarkResponse>> updateMark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody MarkUpdateRequest request) {
        MarkResponse updated = markService.updateMarkByFaculty(userDetails.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Mark updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        markService.deleteMarkByFaculty(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Mark deleted successfully"));
    }
}
