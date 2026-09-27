package com.smartcampus.controller;

import com.smartcampus.dto.request.MarkRequest;
import com.smartcampus.dto.request.MarkUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.MarkResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.service.MarkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Marks administration.
 * ADMIN access only.
 */
@RestController
@RequestMapping("/api/admin/marks")
@PreAuthorize("hasRole('ADMIN')")
public class MarkAdminController {

    private final MarkService markService;

    public MarkAdminController(MarkService markService) {
        this.markService = markService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MarkResponse>>> getMarks(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @PageableDefault(size = 20, sort = "markId") Pageable pageable) {
        PageResponse<MarkResponse> response = markService.getMarksForAdmin(
                examId, studentId, courseId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Marks retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarkResponse>> getMarkById(@PathVariable Long id) {
        MarkResponse response = markService.getMarkById(id);
        return ResponseEntity.ok(ApiResponse.success("Mark retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MarkResponse>> createMark(
            @Valid @RequestBody MarkRequest request) {
        MarkResponse created = markService.createMarkByAdmin(request);
        return new ResponseEntity<>(ApiResponse.success("Mark recorded successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MarkResponse>> updateMark(
            @PathVariable Long id,
            @Valid @RequestBody MarkUpdateRequest request) {
        MarkResponse updated = markService.updateMarkByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Mark updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMark(@PathVariable Long id) {
        markService.deleteMarkByAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Mark deleted successfully"));
    }
}
