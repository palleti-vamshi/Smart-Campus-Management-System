package com.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating or updating a Student.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotNull(message = "Program ID is required")
    private Long programId;

    @NotBlank(message = "Roll number is required")
    @Size(max = 50, message = "Roll number cannot exceed 50 characters")
    private String rollNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Size(max = 20, message = "Gender cannot exceed 20 characters")
    private String gender;

    private LocalDate dateOfBirth;

    @NotNull(message = "Admission year is required")
    @Min(value = 2000, message = "Admission year must be valid")
    @Max(value = 2100, message = "Admission year must be valid")
    private Integer admissionYear;

    @NotNull(message = "Current semester is required")
    @Min(value = 1, message = "Current semester must be at least 1")
    @Max(value = 10, message = "Current semester cannot exceed 10")
    private Integer currentSemester;

    @NotBlank(message = "Section is required")
    @Size(max = 10, message = "Section cannot exceed 10 characters")
    private String section;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;
}
