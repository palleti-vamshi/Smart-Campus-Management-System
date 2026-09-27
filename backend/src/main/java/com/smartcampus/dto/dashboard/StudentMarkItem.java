package com.smartcampus.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentMarkItem {
    private Long markId;
    private String courseCode;
    private String courseName;
    private String examName;
    private String examType;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private String grade;
}
