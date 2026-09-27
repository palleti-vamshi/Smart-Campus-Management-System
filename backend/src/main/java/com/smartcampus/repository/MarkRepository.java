package com.smartcampus.repository;

import com.smartcampus.entity.Mark;
import com.smartcampus.entity.enums.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Mark entity.
 */
@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {

    List<Mark> findByStudent_StudentId(Long studentId);

    List<Mark> findByExam_ExamId(Long examId);

    Optional<Mark> findByExam_ExamIdAndStudent_StudentId(Long examId, Long studentId);

    boolean existsByExam_ExamIdAndStudent_StudentId(Long examId, Long studentId);

    boolean existsByExam_ExamIdAndStudent_StudentIdAndMarkIdNot(Long examId, Long studentId, Long markId);

    boolean existsByStudent_StudentId(Long studentId);

    @Query("SELECT m FROM Mark m WHERE " +
           "(:examId IS NULL OR m.exam.examId = :examId) AND " +
           "(:studentId IS NULL OR m.student.studentId = :studentId) AND " +
           "(:courseId IS NULL OR m.exam.course.courseId = :courseId)")
    Page<Mark> findWithAdminFilters(
            @Param("examId") Long examId,
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            Pageable pageable);

    @Query("SELECT m FROM Mark m WHERE " +
           "m.exam.course.faculty.facultyId = :facultyId AND " +
           "(:examId IS NULL OR m.exam.examId = :examId) AND " +
           "(:studentId IS NULL OR m.student.studentId = :studentId) AND " +
           "(:courseId IS NULL OR m.exam.course.courseId = :courseId)")
    Page<Mark> findWithFacultyFilters(
            @Param("facultyId") Long facultyId,
            @Param("examId") Long examId,
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            Pageable pageable);

    @Query("SELECT m FROM Mark m WHERE " +
           "m.student.studentId = :studentId AND " +
           "(:courseId IS NULL OR m.exam.course.courseId = :courseId) AND " +
           "(:examId IS NULL OR m.exam.examId = :examId) AND " +
           "(:examType IS NULL OR m.exam.examType = :examType) AND " +
           "(:semester IS NULL OR m.exam.course.semester = :semester)")
    Page<Mark> findWithStudentFilters(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("examId") Long examId,
            @Param("examType") ExamType examType,
            @Param("semester") Integer semester,
            Pageable pageable);
}

