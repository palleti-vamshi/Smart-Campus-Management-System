package com.smartcampus.dto.response;

import com.smartcampus.entity.Course;
import com.smartcampus.entity.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Course master data.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long courseId;
    private Long programId;
    private String programCode;
    private String programName;
    private Long facultyId;
    private String facultyEmployeeCode;
    private String facultyName;
    private String courseCode;
    private String courseName;
    private BigDecimal credits;
    private Integer semester;
    private CourseType courseType;
    private String section;
    private Long enrolledStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse fromEntity(Course course) {
        if (course == null) {
            return null;
        }
        String facultyFullName = null;
        if (course.getFaculty() != null) {
            facultyFullName = course.getFaculty().getFirstName() +
                    (course.getFaculty().getLastName() != null ? " " + course.getFaculty().getLastName() : "");
        }

        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .programId(course.getProgram() != null ? course.getProgram().getProgramId() : null)
                .programCode(course.getProgram() != null ? course.getProgram().getProgramCode() : null)
                .programName(course.getProgram() != null ? course.getProgram().getProgramName() : null)
                .facultyId(course.getFaculty() != null ? course.getFaculty().getFacultyId() : null)
                .facultyEmployeeCode(course.getFaculty() != null ? course.getFaculty().getEmployeeCode() : null)
                .facultyName(facultyFullName)
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .courseType(course.getCourseType())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
