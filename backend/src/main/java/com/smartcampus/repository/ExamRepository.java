package com.smartcampus.repository;

import com.smartcampus.entity.Exam;
import com.smartcampus.entity.enums.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the Exam entity.
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCourse_CourseId(Long courseId);

    List<Exam> findByCourse_CourseIdAndExamType(Long courseId, ExamType examType);

    boolean existsByCourse_CourseId(Long courseId);

    @Query("SELECT e FROM Exam e WHERE " +
           "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
           "(:examType IS NULL OR e.examType = :examType) AND " +
           "(:examDate IS NULL OR e.examDate = :examDate) AND " +
           "(:startDate IS NULL OR e.examDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.examDate <= :endDate)")
    Page<Exam> findWithAdminFilters(
            @Param("courseId") Long courseId,
            @Param("examType") ExamType examType,
            @Param("examDate") LocalDate examDate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE " +
           "e.course.faculty.facultyId = :facultyId AND " +
           "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
           "(:examType IS NULL OR e.examType = :examType) AND " +
           "(:examDate IS NULL OR e.examDate = :examDate) AND " +
           "(:startDate IS NULL OR e.examDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.examDate <= :endDate)")
    Page<Exam> findWithFacultyFilters(
            @Param("facultyId") Long facultyId,
            @Param("courseId") Long courseId,
            @Param("examType") ExamType examType,
            @Param("examDate") LocalDate examDate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE " +
           "e.course.courseId IN (SELECT en.course.courseId FROM Enrollment en WHERE en.student.studentId = :studentId) AND " +
           "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
           "(:examType IS NULL OR e.examType = :examType) AND " +
           "(:startDate IS NULL OR e.examDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.examDate <= :endDate)")
    Page<Exam> findWithStudentFilters(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("examType") ExamType examType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}

