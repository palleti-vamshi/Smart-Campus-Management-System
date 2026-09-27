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
public class CourseSummary {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Double credits;
    private Integer semester;
    private String programCode;
    private String facultyName;
}
