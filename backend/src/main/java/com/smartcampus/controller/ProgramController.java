package com.smartcampus.controller;

import com.smartcampus.dto.request.ProgramRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.ProgramResponse;
import com.smartcampus.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Program master data management.
 * Administrative access only.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping("/programs")
    public ResponseEntity<ApiResponse<List<ProgramResponse>>> getAllPrograms() {
        List<ProgramResponse> programs = programService.getAllPrograms();
        return ResponseEntity.ok(ApiResponse.success("Programs retrieved successfully", programs));
    }

    @GetMapping("/programs/{id}")
    public ResponseEntity<ApiResponse<ProgramResponse>> getProgramById(@PathVariable Long id) {
        ProgramResponse program = programService.getProgramById(id);
        return ResponseEntity.ok(ApiResponse.success("Program retrieved successfully", program));
    }

    @GetMapping("/departments/{departmentId}/programs")
    public ResponseEntity<ApiResponse<List<ProgramResponse>>> getProgramsByDepartment(
            @PathVariable Long departmentId) {
        List<ProgramResponse> programs = programService.getProgramsByDepartmentId(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department programs retrieved successfully", programs));
    }

    @PostMapping("/programs")
    public ResponseEntity<ApiResponse<ProgramResponse>> createProgram(
            @Valid @RequestBody ProgramRequest request) {
        ProgramResponse created = programService.createProgram(request);
        return new ResponseEntity<>(ApiResponse.success("Program created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/programs/{id}")
    public ResponseEntity<ApiResponse<ProgramResponse>> updateProgram(
            @PathVariable Long id,
            @Valid @RequestBody ProgramRequest request) {
        ProgramResponse updated = programService.updateProgram(id, request);
        return ResponseEntity.ok(ApiResponse.success("Program updated successfully", updated));
    }

    @DeleteMapping("/programs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok(ApiResponse.success("Program deleted successfully"));
    }
}
