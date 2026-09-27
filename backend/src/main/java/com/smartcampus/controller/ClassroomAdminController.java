package com.smartcampus.controller;

import com.smartcampus.dto.request.ClassroomRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.ClassroomResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.RoomType;
import com.smartcampus.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Classroom administration.
 * ADMIN access only.
 */
@RestController
@RequestMapping("/api/admin/classrooms")
@PreAuthorize("hasRole('ADMIN')")
public class ClassroomAdminController {

    private final ClassroomService classroomService;

    public ClassroomAdminController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClassroomResponse>>> getClassrooms(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "roomNumber") Pageable pageable) {
        PageResponse<ClassroomResponse> response = classroomService.getClassrooms(
                roomNumber, building, roomType, active, pageable);
        return ResponseEntity.ok(ApiResponse.success("Classrooms retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> getClassroomById(@PathVariable Long id) {
        ClassroomResponse response = classroomService.getClassroomById(id);
        return ResponseEntity.ok(ApiResponse.success("Classroom retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassroomResponse>> createClassroom(
            @Valid @RequestBody ClassroomRequest request) {
        ClassroomResponse created = classroomService.createClassroom(request);
        return new ResponseEntity<>(ApiResponse.success("Classroom created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> updateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody ClassroomRequest request) {
        ClassroomResponse updated = classroomService.updateClassroom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Classroom updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.ok(ApiResponse.success("Classroom deleted successfully"));
    }
}
