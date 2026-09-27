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
public class StudentDashboardResponse {

    private StudentProfileSummary profile;

    private StudentQuickSummary quickSummary;

    private AttendanceSummary attendance;

    @Builder.Default
    private List<UpcomingExamSummary> upcomingExams = new ArrayList<>();

    @Builder.Default
    private List<UpcomingExamSummary> pastExams = new ArrayList<>();

    private StudentMarksSummary marks;

    @Builder.Default
    private List<CourseSummary> enrollments = new ArrayList<>();

    private TimetableSummary timetable;

    private NoticeSummary notices;

    private DocumentSummary documentSummary;
}
