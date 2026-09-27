package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.MarkResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.dto.response.StudentResultResponse;
import com.smartcampus.entity.enums.ExamType;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.MarkService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Student Mark and Result operations.
 * STUDENT access only. Read-only access to own marks and results.
 */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentMarkController {

    private final MarkService markService;

    public StudentMarkController(MarkService markService) {
        this.markService = markService;
    }

    @GetMapping("/marks")
    public ResponseEntity<ApiResponse<PageResponse<MarkResponse>>> getMyMarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) ExamType examType,
            @RequestParam(required = false) Integer semester,
            @PageableDefault(size = 20, sort = "markId") Pageable pageable) {
        PageResponse<MarkResponse> response = markService.getMarksForStudent(
                userDetails.getUserId(), courseId, examId, examType, semester, pageable);
        return ResponseEntity.ok(ApiResponse.success("Marks retrieved successfully", response));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<StudentResultResponse>>> getMyResults(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<StudentResultResponse> results = markService.getStudentResults(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Results retrieved successfully", results));
    }
}
