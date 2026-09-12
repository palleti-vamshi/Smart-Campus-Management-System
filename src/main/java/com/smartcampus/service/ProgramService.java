package com.smartcampus.service;

import com.smartcampus.dto.ProgramResponseDTO;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Program;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;

    public ProgramService(
            ProgramRepository programRepository,
            DepartmentRepository departmentRepository) {

        this.programRepository = programRepository;
        this.departmentRepository = departmentRepository;
    }

    public Program createProgram(Long departmentId, Program program) {

        if (programRepository.existsByCode(program.getCode())) {
            throw new IllegalArgumentException(
                    "Program code already exists"
            );
        }

        if (programRepository.existsByName(program.getName())) {
            throw new IllegalArgumentException(
                    "Program name already exists"
            );
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: " + departmentId
                        ));

        program.setDepartment(department);

        return programRepository.save(program);
    }

    public List<ProgramResponseDTO> getAllPrograms() {

        return programRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ProgramResponseDTO getProgramById(Long id) {

        Program program = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Program not found with id: " + id
                        ));

        return convertToDTO(program);
    }

    public List<ProgramResponseDTO> getProgramsByDepartment(
            Long departmentId) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException(
                    "Department not found with id: " + departmentId
            );
        }

        return programRepository.findAll()
                .stream()
                .filter(program ->
                        program.getDepartment()
                                .getId()
                                .equals(departmentId))
                .map(this::convertToDTO)
                .toList();
    }

    public Program updateProgram(
            Long id,
            Program updatedProgram) {

        Program existingProgram = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Program not found with id: " + id
                        ));

        existingProgram.setName(updatedProgram.getName());
        existingProgram.setCode(updatedProgram.getCode());
        existingProgram.setDescription(updatedProgram.getDescription());

        return programRepository.save(existingProgram);
    }

    public void deleteProgram(Long id) {

        Program program = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Program not found with id: " + id
                        ));

        programRepository.delete(program);
    }

    private ProgramResponseDTO convertToDTO(Program program) {

        Department department = program.getDepartment();

        return new ProgramResponseDTO(
                program.getId(),
                program.getName(),
                program.getCode(),
                program.getDescription(),
                department.getId(),
                department.getName(),
                department.getCode()
        );
    }
}