package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchMarkRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotEmpty(message = "Mark entries cannot be empty")
    private List<StudentMarkItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentMarkItem {
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Marks obtained is required")
        private BigDecimal marksObtained;

        private String grade;
        private String remarks;
    }
}
