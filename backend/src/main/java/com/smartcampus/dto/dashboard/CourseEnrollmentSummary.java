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
public class CourseEnrollmentSummary {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private long enrollmentCount;
}
