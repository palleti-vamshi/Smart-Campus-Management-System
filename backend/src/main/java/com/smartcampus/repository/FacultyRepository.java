package com.smartcampus.repository;

import com.smartcampus.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Faculty entity.
 */
@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByEmployeeCode(String employeeCode);

    Optional<Faculty> findByUser_UserId(Long userId);

    List<Faculty> findByDepartment_DepartmentId(Long departmentId);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCodeAndFacultyIdNot(String employeeCode, Long facultyId);

    boolean existsByDepartment_DepartmentId(Long departmentId);

    @Query("SELECT f FROM Faculty f LEFT JOIN f.user u WHERE " +
           "(:departmentId IS NULL OR f.department.departmentId = :departmentId) AND " +
           "(:search IS NULL OR (" +
           "  LOWER(f.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(f.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(f.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(f.specialization) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))" +
           "))")
    Page<Faculty> findWithFilters(
            @Param("departmentId") Long departmentId,
            @Param("search") String search,
            Pageable pageable);
}

