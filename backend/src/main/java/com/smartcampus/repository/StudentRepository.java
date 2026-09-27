package com.smartcampus.repository;

import com.smartcampus.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Student entity.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRollNumber(String rollNumber);

    Optional<Student> findByUser_UserId(Long userId);

    List<Student> findByProgram_ProgramId(Long programId);

    List<Student> findByProgram_ProgramIdAndCurrentSemester(Long programId, Integer currentSemester);

    List<Student> findByProgram_ProgramIdAndCurrentSemesterAndSection(
            Long programId, Integer currentSemester, String section);

    boolean existsByRollNumber(String rollNumber);

    boolean existsByRollNumberAndStudentIdNot(String rollNumber, Long studentId);

    boolean existsByProgram_ProgramId(Long programId);

    @Query("SELECT s FROM Student s LEFT JOIN s.user u WHERE " +
           "(:programId IS NULL OR s.program.programId = :programId) AND " +
           "(:semester IS NULL OR s.currentSemester = :semester) AND " +
           "(:section IS NULL OR LOWER(s.section) = LOWER(:section)) AND " +
           "(:search IS NULL OR (" +
           "  LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))" +
           "))")
    Page<Student> findWithFilters(
            @Param("programId") Long programId,
            @Param("semester") Integer semester,
            @Param("section") String section,
            @Param("search") String search,
            Pageable pageable);
}

