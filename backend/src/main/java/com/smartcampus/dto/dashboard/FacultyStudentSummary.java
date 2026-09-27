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
public class FacultyStudentSummary {
    private long totalStudents;
    private long totalEnrollments;

    @Builder.Default
    private List<CourseEnrollmentSummary> enrollmentsByCourse = new ArrayList<>();
}
