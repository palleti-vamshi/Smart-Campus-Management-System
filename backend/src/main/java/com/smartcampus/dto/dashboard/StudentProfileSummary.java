package com.smartcampus.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileSummary {
    private Long studentId;
    private String rollNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private Long programId;
    private String programCode;
    private String programName;
    private String departmentName;
    private Integer currentSemester;
    private String section;
    private Integer admissionYear;
}
