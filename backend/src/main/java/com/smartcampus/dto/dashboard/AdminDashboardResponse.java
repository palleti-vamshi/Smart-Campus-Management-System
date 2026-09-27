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
public class AdminDashboardResponse {

    private DepartmentSummary departmentSummary;

    @Builder.Default
    private List<ProgramStudentCount> studentsByProgram = new ArrayList<>();

    @Builder.Default
    private List<SemesterStudentCount> studentsBySemester = new ArrayList<>();

    private AttendanceSummary attendanceOverview;

    private AcademicPerformanceSummary academicPerformance;

    private EnrollmentSummary enrollmentSummary;

    private TimetableSummary timetableSummary;

    private NoticeSummary noticeSummary;

    private DocumentSummary documentSummary;

    @Builder.Default
    private List<UpcomingExamSummary> upcomingExams = new ArrayList<>();
}
