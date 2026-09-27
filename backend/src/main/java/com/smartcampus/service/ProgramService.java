package com.smartcampus.service;

import com.smartcampus.dto.request.ProgramRequest;
import com.smartcampus.dto.response.ProgramResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Program;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.CourseRepository;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.ProgramRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling Program master data operations.
 */
@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public ProgramService(ProgramRepository programRepository,
                          DepartmentRepository departmentRepository,
                          StudentRepository studentRepository,
                          CourseRepository courseRepository) {
        this.programRepository = programRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findAll()
                .stream()
                .map(ProgramResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgramById(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));
        return ProgramResponse.fromEntity(program);
    }

    @Transactional(readOnly = true)
    public List<ProgramResponse> getProgramsByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return programRepository.findByDepartment_DepartmentId(departmentId)
                .stream()
                .map(ProgramResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ProgramResponse createProgram(ProgramRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        String code = request.getProgramCode().trim();
        if (programRepository.existsByProgramCode(code)) {
            throw new DuplicateResourceException("Program", "code", code);
        }

        Program program = Program.builder()
                .department(department)
                .programCode(code)
                .programName(request.getProgramName().trim())
                .durationYears(request.getDurationYears())
                .build();

        Program saved = programRepository.save(program);
        return ProgramResponse.fromEntity(saved);
    }

    @Transactional
    public ProgramResponse updateProgram(Long id, ProgramRequest request) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        String code = request.getProgramCode().trim();
        if (programRepository.existsByProgramCodeAndProgramIdNot(code, id)) {
            throw new DuplicateResourceException("Program", "code", code);
        }

        program.setDepartment(department);
        program.setProgramCode(code);
        program.setProgramName(request.getProgramName().trim());
        program.setDurationYears(request.getDurationYears());

        Program updated = programRepository.save(program);
        return ProgramResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteProgram(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));

        if (studentRepository.existsByProgram_ProgramId(id)) {
            throw new ResourceConflictException("Cannot delete program because students are enrolled in it");
        }

        if (courseRepository.existsByProgram_ProgramId(id)) {
            throw new ResourceConflictException("Cannot delete program because courses are assigned to it");
        }

        programRepository.delete(program);
    }
}
