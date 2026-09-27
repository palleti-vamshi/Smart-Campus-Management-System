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
public class DepartmentSummary {
    private long totalStudents;
    private long totalFaculty;
    private long totalPrograms;
    private long totalCourses;
    private long totalClassrooms;
}
