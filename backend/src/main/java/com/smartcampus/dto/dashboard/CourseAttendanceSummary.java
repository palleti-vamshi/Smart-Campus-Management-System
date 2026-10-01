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
public class CourseAttendanceSummary {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseType;
    private long totalClasses;
    private long presentClasses;
    private long absentClasses;
    private long lateClasses;
    private Double attendancePercentage;
    private Double percentage;
    private Double credits;
    private String facultyName;
}
