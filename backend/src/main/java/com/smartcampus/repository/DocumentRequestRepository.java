package com.smartcampus.repository;

import com.smartcampus.entity.DocumentRequest;
import com.smartcampus.entity.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Page<DocumentRequest> findByStudent_StudentId(Long studentId, Pageable pageable);

    Optional<DocumentRequest> findByRequestIdAndStudent_StudentId(Long requestId, Long studentId);

    List<DocumentRequest> findByStatus(DocumentStatus status);

    boolean existsByRequestNumber(String requestNumber);

    boolean existsByVerificationCode(String verificationCode);

    boolean existsByStudent_StudentId(Long studentId);

    boolean existsByDocumentType_DocumentTypeId(Long documentTypeId);

    @Query("""
            SELECT dr
            FROM DocumentRequest dr
            WHERE (:status IS NULL OR dr.status = :status)
              AND (:documentTypeId IS NULL OR dr.documentType.documentTypeId = :documentTypeId)
              AND (:search IS NULL OR (
                    LOWER(dr.requestNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(dr.student.rollNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(dr.student.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(dr.student.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                  ))
            """)
    Page<DocumentRequest> findWithAdminFilters(
            @Param("status") DocumentStatus status,
            @Param("documentTypeId") Long documentTypeId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT dr.status, COUNT(dr) FROM DocumentRequest dr GROUP BY dr.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT dr.status, COUNT(dr) FROM DocumentRequest dr WHERE dr.student.studentId = :studentId GROUP BY dr.status")
    List<Object[]> countByStatusGroupedForStudent(@Param("studentId") Long studentId);
}

