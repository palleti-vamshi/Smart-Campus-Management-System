package com.smartcampus.repository;

import com.smartcampus.entity.DocumentRequest;
import com.smartcampus.entity.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the DocumentRequest entity.
 */
@Repository
public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, Long> {

    Optional<DocumentRequest> findByRequestNumber(String requestNumber);

    Optional<DocumentRequest> findByVerificationCode(String verificationCode);

    List<DocumentRequest> findByStudent_StudentId(Long studentId);

    List<DocumentRequest> findByStatus(DocumentStatus status);

    boolean existsByRequestNumber(String requestNumber);

    boolean existsByStudent_StudentId(Long studentId);
}

