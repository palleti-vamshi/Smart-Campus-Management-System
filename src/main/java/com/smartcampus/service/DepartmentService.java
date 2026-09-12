package com.smartcampus.service;

import com.smartcampus.dto.DepartmentResponseDTO;
import com.smartcampus.entity.Department;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department createDepartment(Department department) {

        if (departmentRepository.existsByCode(department.getCode())) {
            throw new IllegalArgumentException(
                    "Department code already exists"
            );
        }

        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException(
                    "Department name already exists"
            );
        }

        return departmentRepository.save(department);
    }

    public List<DepartmentResponseDTO> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public DepartmentResponseDTO getDepartmentById(Long id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: " + id
                        ));

        return convertToDTO(department);
    }

    public Department updateDepartment(
            Long id,
            Department updatedDepartment) {

        Department existingDepartment = getDepartmentEntityById(id);

        existingDepartment.setName(updatedDepartment.getName());
        existingDepartment.setCode(updatedDepartment.getCode());
        existingDepartment.setDescription(
                updatedDepartment.getDescription()
        );

        return departmentRepository.save(existingDepartment);
    }

    public void deleteDepartment(Long id) {

        Department department = getDepartmentEntityById(id);

        departmentRepository.delete(department);
    }

    private Department getDepartmentEntityById(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: " + id
                        ));
    }

    private DepartmentResponseDTO convertToDTO(
            Department department) {

        return new DepartmentResponseDTO(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription()
        );
    }
}