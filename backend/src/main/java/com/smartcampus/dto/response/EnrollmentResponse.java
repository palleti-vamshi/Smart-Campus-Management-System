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

    private String courseType;
    private java.math.BigDecimal credits;
    private String facultyName;
    private String programCode;
    private String programName;
    private String section;

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        String studentName = null;
        String rollNumber = null;
        String section = null;
        Long sId = null;
        if (enrollment.getStudent() != null) {
            sId = enrollment.getStudent().getStudentId();
            rollNumber = enrollment.getStudent().getRollNumber();
            section = enrollment.getStudent().getSection();
            studentName = (enrollment.getStudent().getFirstName() != null ? enrollment.getStudent().getFirstName() : "")
                    + (enrollment.getStudent().getLastName() != null ? " " + enrollment.getStudent().getLastName() : "");
            studentName = studentName.trim();
        }

        Long cId = null;
        String code = null;
        String name = null;
        String cType = null;
        java.math.BigDecimal creds = null;
        String facName = null;
        String progCode = null;
        String progName = null;

        if (enrollment.getCourse() != null) {
            cId = enrollment.getCourse().getCourseId();
            code = enrollment.getCourse().getCourseCode();
            name = enrollment.getCourse().getCourseName();
            if (enrollment.getCourse().getCourseType() != null) {
                cType = enrollment.getCourse().getCourseType().name();
            }
            creds = enrollment.getCourse().getCredits();
            if (enrollment.getCourse().getFaculty() != null) {
                facName = (enrollment.getCourse().getFaculty().getFirstName() != null ? enrollment.getCourse().getFaculty().getFirstName() : "")
                        + (enrollment.getCourse().getFaculty().getLastName() != null ? " " + enrollment.getCourse().getFaculty().getLastName() : "");
                facName = facName.trim();
            }
            if (enrollment.getCourse().getProgram() != null) {
                progCode = enrollment.getCourse().getProgram().getProgramCode();
                progName = enrollment.getCourse().getProgram().getProgramName();
            }
        }

        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .studentId(sId)
                .studentRollNumber(rollNumber)
                .studentName(studentName)
                .section(section)
                .courseId(cId)
                .courseCode(code)
                .courseName(name)
                .courseType(cType)
                .credits(creds)
                .facultyName(facName)
                .programCode(progCode)
                .programName(progName)
                .academicYear(enrollment.getAcademicYear())
                .semester(enrollment.getSemester())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .status(enrollment.getStatus())
                .build();
    }
}
