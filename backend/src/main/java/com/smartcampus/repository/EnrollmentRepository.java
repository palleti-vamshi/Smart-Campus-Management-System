package com.smartcampus.repository;

import com.smartcampus.entity.Enrollment;
import com.smartcampus.entity.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Enrollment entity.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent_StudentId(Long studentId);

    List<Enrollment> findByCourse_CourseId(Long courseId);

    Optional<Enrollment> findByStudent_StudentIdAndCourse_CourseIdAndAcademicYearAndSemester(
            Long studentId, Long courseId, String academicYear, Integer semester);

    boolean existsByStudent_StudentIdAndCourse_CourseIdAndAcademicYearAndSemester(
            Long studentId, Long courseId, String academicYear, Integer semester);

    boolean existsByStudent_StudentIdAndCourse_CourseIdAndAcademicYearAndSemesterAndEnrollmentIdNot(
            Long studentId, Long courseId, String academicYear, Integer semester, Long enrollmentId);

    boolean existsByStudent_StudentId(Long studentId);

    boolean existsByCourse_CourseId(Long courseId);

    boolean existsByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE " +
           "(:studentId IS NULL OR e.student.studentId = :studentId) AND " +
           "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
           "(:academicYear IS NULL OR e.academicYear = :academicYear) AND " +
           "(:semester IS NULL OR e.semester = :semester) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Enrollment> findWithAdminFilters(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester,
            @Param("status") EnrollmentStatus status,
            Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE " +
           "e.course.faculty.facultyId = :facultyId AND " +
           "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
           "(:academicYear IS NULL OR e.academicYear = :academicYear) AND " +
           "(:semester IS NULL OR e.semester = :semester) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Enrollment> findWithFacultyFilters(
            @Param("facultyId") Long facultyId,
            @Param("courseId") Long courseId,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester,
            @Param("status") EnrollmentStatus status,
            Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE " +
           "e.student.studentId = :studentId AND " +
           "(:academicYear IS NULL OR e.academicYear = :academicYear) AND " +
           "(:semester IS NULL OR e.semester = :semester) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Enrollment> findWithStudentFilters(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester,
            @Param("status") EnrollmentStatus status,
            Pageable pageable);

    long countByStatus(EnrollmentStatus status);

    @Query("SELECT p.programId, p.programCode, p.programName, COUNT(e) " +
           "FROM Enrollment e JOIN e.student s JOIN s.program p " +
           "GROUP BY p.programId, p.programCode, p.programName ORDER BY p.programName")
    List<Object[]> countEnrollmentsByProgram();

    @Query("SELECT e.course.courseId, e.course.courseCode, e.course.courseName, COUNT(e) " +
           "FROM Enrollment e WHERE e.course.courseId IN :courseIds AND e.status = com.smartcampus.entity.enums.EnrollmentStatus.ACTIVE " +
           "GROUP BY e.course.courseId, e.course.courseCode, e.course.courseName ORDER BY e.course.courseName")
    List<Object[]> countActiveEnrollmentsForCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COUNT(DISTINCT e.student.studentId) FROM Enrollment e " +
           "WHERE e.course.courseId IN :courseIds AND e.status = com.smartcampus.entity.enums.EnrollmentStatus.ACTIVE")
    long countDistinctStudentsForCourseIds(@Param("courseIds") List<Long> courseIds);
}


