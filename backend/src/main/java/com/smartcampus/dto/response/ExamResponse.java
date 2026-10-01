package com.smartcampus.dto.response;

import com.smartcampus.entity.Exam;
import com.smartcampus.entity.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {

    private Long examId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String examName;
    private ExamType examType;
    private LocalDate examDate;
    private BigDecimal maxMarks;
    private String courseType;
    private LocalDateTime createdAt;

    public static ExamResponse fromEntity(Exam exam) {
        if (exam == null) {
            return null;
        }

        Long cId = null;
        String cCode = null;
        String cName = null;
        String cType = null;
        if (exam.getCourse() != null) {
            cId = exam.getCourse().getCourseId();
            cCode = exam.getCourse().getCourseCode();
            cName = exam.getCourse().getCourseName();
            cType = exam.getCourse().getCourseType() != null ? exam.getCourse().getCourseType().name() : null;
        }

        return ExamResponse.builder()
                .examId(exam.getExamId())
                .courseId(cId)
                .courseCode(cCode)
                .courseName(cName)
                .courseType(cType)
                .examName(exam.getExamName())
                .examType(exam.getExamType())
                .examDate(exam.getExamDate())
                .maxMarks(exam.getMaxMarks())
                .createdAt(exam.getCreatedAt())
                .build();
    }
}
