package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.ExamResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.ExamType;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.ExamService;
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

/**
 * Controller for Student Exam operations.
 * STUDENT access only. Read-only access to exams for enrolled courses.
 */
@RestController
@RequestMapping("/api/student/exams")
@PreAuthorize("hasRole('STUDENT')")
public class StudentExamController {

    private final ExamService examService;

    public StudentExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExamResponse>>> getMyExams(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) ExamType examType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "examDate") Pageable pageable) {
        PageResponse<ExamResponse> response = examService.getExamsForStudent(
                userDetails.getUserId(), courseId, examType, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Exams retrieved successfully", response));
    }
}
