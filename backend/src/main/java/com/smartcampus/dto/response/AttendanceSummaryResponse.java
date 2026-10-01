package com.smartcampus.dto.response;

import com.smartcampus.entity.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {

    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseType;
    private Long totalClasses;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Double attendancePercentage;

    public AttendanceSummaryResponse(
            Long courseId,
            String courseCode,
            String courseName,
            CourseType courseType,
            Long totalClasses,
            Long presentCount,
            Long absentCount,
            Long lateCount) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.courseType = courseType != null ? courseType.name() : null;
        this.totalClasses = totalClasses != null ? totalClasses : 0L;
        this.presentCount = presentCount != null ? presentCount : 0L;
        this.absentCount = absentCount != null ? absentCount : 0L;
        this.lateCount = lateCount != null ? lateCount : 0L;

        if (this.totalClasses > 0) {
            double pct = ((double) this.presentCount / this.totalClasses) * 100.0;
            this.attendancePercentage = BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP).doubleValue();
        } else {
            this.attendancePercentage = 0.0;
        }
    }

    public AttendanceSummaryResponse(
            Long courseId,
            String courseCode,
            String courseName,
            Long totalClasses,
            Long presentCount,
            Long absentCount,
            Long lateCount) {
        this(courseId, courseCode, courseName, (CourseType) null, totalClasses, presentCount, absentCount, lateCount);
    }
}
