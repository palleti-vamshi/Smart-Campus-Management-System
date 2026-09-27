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
public class FacultyMarksSummary {
    private long marksEnteredCount;

    @Builder.Default
    private List<CoursePerformanceSummary> coursePerformance = new ArrayList<>();
}
