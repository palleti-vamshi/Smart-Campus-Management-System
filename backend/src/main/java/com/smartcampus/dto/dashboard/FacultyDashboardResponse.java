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
public class FacultyDashboardResponse {

    private Long facultyId;
    private String facultyName;
    private String employeeCode;
    private String designation;
    private String departmentName;

    private int totalAssignedCourses;

    @Builder.Default
    private List<CourseSummary> assignedCourses = new ArrayList<>();

    private FacultyStudentSummary studentEnrollmentSummary;

    private AttendanceSummary attendanceOverview;

    private FacultyExamSummary examSummary;

    private FacultyMarksSummary marksSummary;

    private TimetableSummary timetableSummary;

    private NoticeSummary noticeSummary;
}
