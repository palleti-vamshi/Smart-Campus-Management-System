package com.smartcampus.dto.response;

import com.smartcampus.entity.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResultResponse {

    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseType;
    private BigDecimal credits;
    private Integer semester;
    private Integer totalExams;
    private BigDecimal totalMarksObtained;
    private BigDecimal totalMaxMarks;
    private Double percentage;
    private List<ExamResultItem> examResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamResultItem {
        private Long examId;
        private String examName;
        private ExamType examType;
        private LocalDate examDate;
        private BigDecimal maxMarks;
        private BigDecimal marksObtained;
        private String grade;
    }
}
