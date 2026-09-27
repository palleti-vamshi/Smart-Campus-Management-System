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
public class StudentQuickSummary {
    private Double overallAttendancePercentage;
    private long upcomingExamsCount;
    private long pendingDocumentRequests;
    private long activeNoticesCount;
    private long enrolledCoursesCount;
}
