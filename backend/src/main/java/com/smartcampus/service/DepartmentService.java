package com.smartcampus.service;

import com.smartcampus.dto.request.DepartmentRequest;
import com.smartcampus.dto.response.DepartmentResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling Department master data operations.
 */
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final FacultyRepository facultyRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             ProgramRepository programRepository,
                             FacultyRepository facultyRepository) {
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return DepartmentResponse.fromEntity(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentCode(request.getDepartmentCode())) {
            throw new DuplicateResourceException("Department", "code", request.getDepartmentCode());
        }

        Department department = Department.builder()
                .departmentCode(request.getDepartmentCode().trim().toUpperCase())
                .departmentName(request.getDepartmentName().trim())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        return DepartmentResponse.fromEntity(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        String newCode = request.getDepartmentCode().trim().toUpperCase();
        if (departmentRepository.existsByDepartmentCodeAndDepartmentIdNot(newCode, id)) {
            throw new DuplicateResourceException("Department", "code", newCode);
        }

        department.setDepartmentCode(newCode);
        department.setDepartmentName(request.getDepartmentName().trim());
        department.setDescription(request.getDescription());

        Department updated = departmentRepository.save(department);
        return DepartmentResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (programRepository.existsByDepartment_DepartmentId(id)) {
            throw new ResourceConflictException("Cannot delete department because academic programs are associated with it");
        }

        if (facultyRepository.existsByDepartment_DepartmentId(id)) {
            throw new ResourceConflictException("Cannot delete department because faculty members are assigned to it");
        }

        departmentRepository.delete(department);
    }
}
