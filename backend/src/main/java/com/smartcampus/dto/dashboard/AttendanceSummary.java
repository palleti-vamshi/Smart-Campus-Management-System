package com.smartcampus.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummary {
    private long totalClasses;
    private long presentClasses;
    private long absentClasses;
    private long lateClasses;
    private Double overallPercentage;

    @Builder.Default
    private List<ProgramAttendanceSummary> byProgram = new ArrayList<>();

    @Builder.Default
    private List<CourseAttendanceSummary> byCourse = new ArrayList<>();
}
