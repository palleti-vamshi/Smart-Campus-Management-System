package com.smartcampus.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingExamSummary {
    private Long examId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String examName;
    private String examType;
    private String courseType;
    private LocalDate examDate;
    private BigDecimal maxMarks;
}
