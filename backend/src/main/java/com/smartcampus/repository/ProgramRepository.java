package com.smartcampus.repository;

import com.smartcampus.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Program entity.
 */
@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByProgramCode(String programCode);

    List<Program> findByDepartment_DepartmentId(Long departmentId);

    boolean existsByProgramCode(String programCode);

    boolean existsByProgramCodeAndProgramIdNot(String programCode, Long programId);

    boolean existsByDepartment_DepartmentId(Long departmentId);
}

