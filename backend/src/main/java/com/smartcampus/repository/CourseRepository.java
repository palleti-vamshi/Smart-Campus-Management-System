package com.smartcampus.repository;

import com.smartcampus.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Course entity.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    List<Course> findByProgram_ProgramId(Long programId);

    List<Course> findByProgram_ProgramIdAndSemester(Long programId, Integer semester);

    List<Course> findByFaculty_FacultyId(Long facultyId);

    boolean existsByCourseCode(String courseCode);

    boolean existsByCourseCodeAndCourseIdNot(String courseCode, Long courseId);

    boolean existsByProgram_ProgramId(Long programId);

    boolean existsByFaculty_FacultyId(Long facultyId);

    @Query("SELECT c FROM Course c LEFT JOIN c.faculty f WHERE " +
           "(:programId IS NULL OR c.program.programId = :programId) AND " +
           "(:semester IS NULL OR c.semester = :semester) AND " +
           "(:facultyId IS NULL OR (c.faculty IS NOT NULL AND c.faculty.facultyId = :facultyId)) AND " +
           "(:search IS NULL OR (" +
           "  LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.courseName) LIKE LOWER(CONCAT('%', :search, '%'))" +
           "))")
    Page<Course> findWithFilters(
            @Param("programId") Long programId,
            @Param("semester") Integer semester,
            @Param("facultyId") Long facultyId,
            @Param("search") String search,
            Pageable pageable);
}

