package com.smartcampus.dto.response;

import com.smartcampus.entity.Program;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Program master data.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {

    private Long programId;
    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private String programCode;
    private String programName;
    private Integer durationYears;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProgramResponse fromEntity(Program program) {
        if (program == null) {
            return null;
        }
        return ProgramResponse.builder()
                .programId(program.getProgramId())
                .departmentId(program.getDepartment() != null ? program.getDepartment().getDepartmentId() : null)
                .departmentCode(program.getDepartment() != null ? program.getDepartment().getDepartmentCode() : null)
                .departmentName(program.getDepartment() != null ? program.getDepartment().getDepartmentName() : null)
                .programCode(program.getProgramCode())
                .programName(program.getProgramName())
                .durationYears(program.getDurationYears())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();
    }
}
