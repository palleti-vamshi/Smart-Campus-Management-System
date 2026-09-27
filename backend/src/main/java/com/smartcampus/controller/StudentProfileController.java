package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Student self-service operations.
 * Restricted to authenticated STUDENT users.
 */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

    private final StudentService studentService;

    public StudentProfileController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Retrieves the authenticated student's own profile.
     * Uses userId from SecurityContext to guarantee the caller can never access another student's record.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<StudentResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StudentResponse profile = studentService.getStudentByUserId(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Student profile retrieved successfully", profile));
    }
}
