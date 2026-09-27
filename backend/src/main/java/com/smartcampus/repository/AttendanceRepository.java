package com.smartcampus.repository;

import com.smartcampus.dto.response.AttendanceSummaryResponse;
import com.smartcampus.entity.Attendance;
import com.smartcampus.entity.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Attendance entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);

    List<Attendance> findByCourse_CourseIdAndAttendanceDate(Long courseId, LocalDate attendanceDate);

    Optional<Attendance> findByStudent_StudentIdAndCourse_CourseIdAndAttendanceDate(
            Long studentId, Long courseId, LocalDate attendanceDate);

    boolean existsByStudent_StudentIdAndCourse_CourseIdAndAttendanceDate(
            Long studentId, Long courseId, LocalDate attendanceDate);

    boolean existsByStudent_StudentIdAndCourse_CourseIdAndAttendanceDateAndAttendanceIdNot(
            Long studentId, Long courseId, LocalDate attendanceDate, Long attendanceId);

    boolean existsByStudent_StudentId(Long studentId);

    boolean existsByCourse_CourseId(Long courseId);

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:courseId IS NULL OR a.course.courseId = :courseId) AND " +
           "(:studentId IS NULL OR a.student.studentId = :studentId) AND " +
           "(:facultyId IS NULL OR a.course.faculty.facultyId = :facultyId) AND " +
           "(:attendanceDate IS NULL OR a.attendanceDate = :attendanceDate) AND " +
           "(:startDate IS NULL OR a.attendanceDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.attendanceDate <= :endDate) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Attendance> findWithAdminFilters(
            @Param("courseId") Long courseId,
            @Param("studentId") Long studentId,
            @Param("facultyId") Long facultyId,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE " +
           "a.course.faculty.facultyId = :facultyId AND " +
           "(:courseId IS NULL OR a.course.courseId = :courseId) AND " +
           "(:studentId IS NULL OR a.student.studentId = :studentId) AND " +
           "(:attendanceDate IS NULL OR a.attendanceDate = :attendanceDate) AND " +
           "(:startDate IS NULL OR a.attendanceDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.attendanceDate <= :endDate) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Attendance> findWithFacultyFilters(
            @Param("facultyId") Long facultyId,
            @Param("courseId") Long courseId,
            @Param("studentId") Long studentId,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE " +
           "a.student.studentId = :studentId AND " +
           "(:courseId IS NULL OR a.course.courseId = :courseId) AND " +
           "(:attendanceDate IS NULL OR a.attendanceDate = :attendanceDate) AND " +
           "(:startDate IS NULL OR a.attendanceDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.attendanceDate <= :endDate) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Attendance> findWithStudentFilters(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    @Query("SELECT new com.smartcampus.dto.response.AttendanceSummaryResponse(" +
           "e.course.courseId, e.course.courseCode, e.course.courseName, " +
           "COUNT(a.attendanceId), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.PRESENT THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.ABSENT THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.LATE THEN 1L ELSE 0L END)) " +
           "FROM Enrollment e " +
           "LEFT JOIN Attendance a ON a.course = e.course AND a.student = e.student " +
           "WHERE e.student.studentId = :studentId " +
           "GROUP BY e.course.courseId, e.course.courseCode, e.course.courseName")
    List<AttendanceSummaryResponse> getStudentAttendanceSummary(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(a), SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.PRESENT THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.ABSENT THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.LATE THEN 1L ELSE 0L END) " +
           "FROM Attendance a")
    List<Object[]> getOverallAttendanceStats();

    @Query("SELECT p.programId, p.programCode, p.programName, COUNT(a), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.PRESENT THEN 1L ELSE 0L END) " +
           "FROM Attendance a JOIN a.student s JOIN s.program p " +
           "GROUP BY p.programId, p.programCode, p.programName ORDER BY p.programName")
    List<Object[]> getAttendanceStatsByProgram();

    @Query("SELECT a.course.courseId, a.course.courseCode, a.course.courseName, COUNT(a), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.PRESENT THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN a.status = com.smartcampus.entity.enums.AttendanceStatus.ABSENT THEN 1L ELSE 0L END) " +
           "FROM Attendance a WHERE a.course.courseId IN :courseIds " +
           "GROUP BY a.course.courseId, a.course.courseCode, a.course.courseName ORDER BY a.course.courseName")
    List<Object[]> getAttendanceStatsForCourseIds(@Param("courseIds") List<Long> courseIds);
}



