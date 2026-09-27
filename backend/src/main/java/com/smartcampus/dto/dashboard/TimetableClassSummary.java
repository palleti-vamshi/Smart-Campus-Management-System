package com.smartcampus.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableClassSummary {
    private Long timetableId;
    private String courseCode;
    private String courseName;
    private String roomNumber;
    private String building;
    private String facultyName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String dayOfWeek;
    private Integer semester;
}
