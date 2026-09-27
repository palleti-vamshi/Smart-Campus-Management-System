package com.smartcampus.controller;

import com.smartcampus.dto.request.ExamRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.ExamResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.ExamType;
import com.smartcampus.service.ExamService;
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
 * Controller for Exam administration.
 * ADMIN access only.
 */
@RestController
@RequestMapping("/api/admin/exams")
@PreAuthorize("hasRole('ADMIN')")
public class ExamAdminController {

    private final ExamService examService;

    public ExamAdminController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExamResponse>>> getExams(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) ExamType examType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "examId") Pageable pageable) {
        PageResponse<ExamResponse> response = examService.getExamsForAdmin(
                courseId, examType, examDate, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Exams retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(@PathVariable Long id) {
        ExamResponse response = examService.getExamById(id);
        return ResponseEntity.ok(ApiResponse.success("Exam retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @Valid @RequestBody ExamRequest request) {
        ExamResponse created = examService.createExamByAdmin(request);
        return new ResponseEntity<>(ApiResponse.success("Exam created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(
            @PathVariable Long id,
            @Valid @RequestBody ExamRequest request) {
        ExamResponse updated = examService.updateExamByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Exam updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        examService.deleteExamByAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Exam deleted successfully"));
    }
}
