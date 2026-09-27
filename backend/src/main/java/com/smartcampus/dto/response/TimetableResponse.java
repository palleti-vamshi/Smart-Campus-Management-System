package com.smartcampus.dto.response;

import com.smartcampus.entity.Timetable;
import com.smartcampus.entity.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponse {

    private Long timetableId;

    // Program details
    private Long programId;
    private String programCode;
    private String programName;

    // Course details
    private Long courseId;
    private String courseCode;
    private String courseName;

    // Faculty details
    private Long facultyId;
    private String facultyName;
    private String facultyEmployeeCode;

    // Classroom details
    private Long classroomId;
    private String roomNumber;
    private String building;
    private RoomType roomType;

    // Schedule details
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer semester;
    private String academicYear;

    public static TimetableResponse fromEntity(Timetable timetable) {
        if (timetable == null) {
            return null;
        }

        String facultyFullName = null;
        String facultyEmpCode = null;
        Long facultyId = null;
        if (timetable.getFaculty() != null) {
            facultyId = timetable.getFaculty().getFacultyId();
            facultyEmpCode = timetable.getFaculty().getEmployeeCode();
            facultyFullName = (timetable.getFaculty().getFirstName() +
                    (timetable.getFaculty().getLastName() != null ? " " + timetable.getFaculty().getLastName() : "")).trim();
        }

        return TimetableResponse.builder()
                .timetableId(timetable.getTimetableId())
                .programId(timetable.getProgram() != null ? timetable.getProgram().getProgramId() : null)
                .programCode(timetable.getProgram() != null ? timetable.getProgram().getProgramCode() : null)
                .programName(timetable.getProgram() != null ? timetable.getProgram().getProgramName() : null)
                .courseId(timetable.getCourse() != null ? timetable.getCourse().getCourseId() : null)
                .courseCode(timetable.getCourse() != null ? timetable.getCourse().getCourseCode() : null)
                .courseName(timetable.getCourse() != null ? timetable.getCourse().getCourseName() : null)
                .facultyId(facultyId)
                .facultyName(facultyFullName)
                .facultyEmployeeCode(facultyEmpCode)
                .classroomId(timetable.getClassroom() != null ? timetable.getClassroom().getClassroomId() : null)
                .roomNumber(timetable.getClassroom() != null ? timetable.getClassroom().getRoomNumber() : null)
                .building(timetable.getClassroom() != null ? timetable.getClassroom().getBuilding() : null)
                .roomType(timetable.getClassroom() != null ? timetable.getClassroom().getRoomType() : null)
                .dayOfWeek(timetable.getDayOfWeek())
                .startTime(timetable.getStartTime())
                .endTime(timetable.getEndTime())
                .semester(timetable.getSemester())
                .academicYear(timetable.getAcademicYear())
                .build();
    }
}
