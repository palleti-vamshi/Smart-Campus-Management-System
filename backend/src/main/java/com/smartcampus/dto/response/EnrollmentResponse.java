package com.smartcampus.dto.response;

import com.smartcampus.entity.Enrollment;
import com.smartcampus.entity.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long enrollmentId;
    private Long studentId;
    private String studentRollNumber;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String academicYear;
    private Integer semester;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        String studentName = null;
        String rollNumber = null;
        Long sId = null;
        if (enrollment.getStudent() != null) {
            sId = enrollment.getStudent().getStudentId();
            rollNumber = enrollment.getStudent().getRollNumber();
            studentName = (enrollment.getStudent().getFirstName() != null ? enrollment.getStudent().getFirstName() : "")
                    + (enrollment.getStudent().getLastName() != null ? " " + enrollment.getStudent().getLastName() : "");
            studentName = studentName.trim();
        }

        Long cId = null;
        String code = null;
        String name = null;
        if (enrollment.getCourse() != null) {
            cId = enrollment.getCourse().getCourseId();
            code = enrollment.getCourse().getCourseCode();
            name = enrollment.getCourse().getCourseName();
        }

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .studentId(sId)
                .studentRollNumber(rollNumber)
                .studentName(studentName)
                .courseId(cId)
                .courseCode(code)
                .courseName(name)
                .academicYear(enrollment.getAcademicYear())
                .semester(enrollment.getSemester())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .status(enrollment.getStatus())
                .build();
    }
}
