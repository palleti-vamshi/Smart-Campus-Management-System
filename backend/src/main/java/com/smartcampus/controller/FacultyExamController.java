package com.smartcampus.controller;

import com.smartcampus.dto.request.ExamRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.ExamResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.ExamType;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.ExamService;
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
 * Controller for Faculty Exam operations.
 * FACULTY access only. Restricted to courses taught by the authenticated faculty member.
 */
@RestController
@RequestMapping("/api/faculty/exams")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyExamController {

    private final ExamService examService;

    public FacultyExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExamResponse>>> getExams(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) ExamType examType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "examId") Pageable pageable) {
        PageResponse<ExamResponse> response = examService.getExamsForFaculty(
                userDetails.getUserId(), courseId, examType, examDate, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Exams retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExamRequest request) {
        ExamResponse created = examService.createExamByFaculty(userDetails.getUserId(), request);
        return new ResponseEntity<>(ApiResponse.success("Exam created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ExamRequest request) {
        ExamResponse updated = examService.updateExamByFaculty(userDetails.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Exam updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExam(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        examService.deleteExamByFaculty(userDetails.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Exam deleted successfully"));
    }
}
