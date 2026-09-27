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
public class ProgramAttendanceSummary {
    private Long programId;
    private String programCode;
    private String programName;
    private long totalRecords;
    private long presentRecords;
    private Double attendancePercentage;
}
