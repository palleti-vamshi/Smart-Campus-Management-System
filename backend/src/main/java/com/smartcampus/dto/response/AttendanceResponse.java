package com.smartcampus.dto.response;

import com.smartcampus.entity.Attendance;
import com.smartcampus.entity.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long attendanceId;
    private Long studentId;
    private String studentRollNumber;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private Long markedByFacultyId;
    private String markedByFacultyName;
    private LocalDateTime createdAt;

    public static AttendanceResponse fromEntity(Attendance attendance) {
        if (attendance == null) {
            return null;
        }

        Long sId = null;
        String rollNumber = null;
        String sName = null;
        if (attendance.getStudent() != null) {
            sId = attendance.getStudent().getStudentId();
            rollNumber = attendance.getStudent().getRollNumber();
            sName = (attendance.getStudent().getFirstName() != null ? attendance.getStudent().getFirstName() : "")
                    + (attendance.getStudent().getLastName() != null ? " " + attendance.getStudent().getLastName() : "");
            sName = sName.trim();
        }

        Long cId = null;
        String cCode = null;
        String cName = null;
        if (attendance.getCourse() != null) {
            cId = attendance.getCourse().getCourseId();
            cCode = attendance.getCourse().getCourseCode();
            cName = attendance.getCourse().getCourseName();
        }

        Long fId = null;
        String fName = null;
        if (attendance.getMarkedBy() != null) {
            fId = attendance.getMarkedBy().getFacultyId();
            fName = (attendance.getMarkedBy().getFirstName() != null ? attendance.getMarkedBy().getFirstName() : "")
                    + (attendance.getMarkedBy().getLastName() != null ? " " + attendance.getMarkedBy().getLastName() : "");
            fName = fName.trim();
        }

        return AttendanceResponse.builder()
                .attendanceId(attendance.getAttendanceId())
                .studentId(sId)
                .studentRollNumber(rollNumber)
                .studentName(sName)
                .courseId(cId)
                .courseCode(cCode)
                .courseName(cName)
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus())
                .markedByFacultyId(fId)
                .markedByFacultyName(fName)
                .createdAt(attendance.getCreatedAt())
                .build();
    }
}
