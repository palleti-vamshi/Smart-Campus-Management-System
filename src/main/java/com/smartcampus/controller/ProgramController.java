package com.smartcampus.controller;

import com.smartcampus.dto.ProgramResponseDTO;
import com.smartcampus.entity.Program;
import com.smartcampus.service.ProgramService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/department/{departmentId}")
    public ResponseEntity<Program> createProgram(
            @PathVariable Long departmentId,
            @RequestBody Program program) {

        Program createdProgram =
                programService.createProgram(departmentId, program);

        return new ResponseEntity<>(
                createdProgram,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponseDTO>> getAllPrograms() {

        return ResponseEntity.ok(
                programService.getAllPrograms()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponseDTO> getProgramById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                programService.getProgramById(id)
        );
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<ProgramResponseDTO>>
    getProgramsByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(
                programService.getProgramsByDepartment(departmentId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Program> updateProgram(
            @PathVariable Long id,
            @RequestBody Program program) {

        return ResponseEntity.ok(
                programService.updateProgram(id, program)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable Long id) {

        programService.deleteProgram(id);

        return ResponseEntity.noContent().build();
    }
}