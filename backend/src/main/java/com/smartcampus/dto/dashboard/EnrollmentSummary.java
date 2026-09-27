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
public class EnrollmentSummary {
    private long totalEnrollments;
    private long activeEnrollments;

    @Builder.Default
    private List<ProgramEnrollmentSummary> byProgram = new ArrayList<>();

    @Builder.Default
    private List<CourseEnrollmentSummary> byCourse = new ArrayList<>();
}
