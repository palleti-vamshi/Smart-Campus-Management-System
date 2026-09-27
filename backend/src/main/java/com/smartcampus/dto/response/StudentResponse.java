package com.smartcampus.dto.response;

import com.smartcampus.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Student master data.
 * Does not expose password hashes or sensitive security credentials.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long studentId;
    private Long userId;
    private String username;
    private String email;
    private Long programId;
    private String programCode;
    private String programName;
    private String rollNumber;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private Integer admissionYear;
    private Integer currentSemester;
    private String section;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StudentResponse fromEntity(Student student) {
        if (student == null) {
            return null;
        }
        return StudentResponse.builder()
                .studentId(student.getStudentId())
                .userId(student.getUser() != null ? student.getUser().getUserId() : null)
                .username(student.getUser() != null ? student.getUser().getUsername() : null)
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .programId(student.getProgram() != null ? student.getProgram().getProgramId() : null)
                .programCode(student.getProgram() != null ? student.getProgram().getProgramCode() : null)
                .programName(student.getProgram() != null ? student.getProgram().getProgramName() : null)
                .rollNumber(student.getRollNumber())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .admissionYear(student.getAdmissionYear())
                .currentSemester(student.getCurrentSemester())
                .section(student.getSection())
                .phone(student.getPhone())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
