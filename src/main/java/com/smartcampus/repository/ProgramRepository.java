package com.smartcampus.repository;

import com.smartcampus.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}