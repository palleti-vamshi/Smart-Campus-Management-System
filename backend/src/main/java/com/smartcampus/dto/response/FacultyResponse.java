package com.smartcampus.dto.response;

import com.smartcampus.entity.Faculty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Faculty master data.
 * Does not expose password hashes or sensitive security credentials.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResponse {

    private Long facultyId;
    private Long userId;
    private String username;
    private String email;
    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String designation;
    private String specialization;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FacultyResponse fromEntity(Faculty faculty) {
        if (faculty == null) {
            return null;
        }
        return FacultyResponse.builder()
                .facultyId(faculty.getFacultyId())
                .userId(faculty.getUser() != null ? faculty.getUser().getUserId() : null)
                .username(faculty.getUser() != null ? faculty.getUser().getUsername() : null)
                .email(faculty.getUser() != null ? faculty.getUser().getEmail() : null)
                .departmentId(faculty.getDepartment() != null ? faculty.getDepartment().getDepartmentId() : null)
                .departmentCode(faculty.getDepartment() != null ? faculty.getDepartment().getDepartmentCode() : null)
                .departmentName(faculty.getDepartment() != null ? faculty.getDepartment().getDepartmentName() : null)
                .employeeCode(faculty.getEmployeeCode())
                .firstName(faculty.getFirstName())
                .lastName(faculty.getLastName())
                .designation(faculty.getDesignation())
                .specialization(faculty.getSpecialization())
                .phone(faculty.getPhone())
                .createdAt(faculty.getCreatedAt())
                .updatedAt(faculty.getUpdatedAt())
                .build();
    }
}
