package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.CourseResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Faculty Course operations.
 * FACULTY access only. Restricted to courses assigned to the authenticated faculty.
 */
@RestController
@RequestMapping("/api/faculty/courses")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyCourseController {

    private final CourseService courseService;

    public FacultyCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getMyAssignedCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CourseResponse> courses = courseService.getCoursesForFaculty(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Assigned courses retrieved successfully", courses));
    }
}
