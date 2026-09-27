package com.smartcampus.repository;

import com.smartcampus.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the DocumentType entity.
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {

    Optional<DocumentType> findByDocumentName(String documentName);

    List<DocumentType> findByIsActiveTrue();

    boolean existsByDocumentName(String documentName);

    boolean existsByDocumentNameAndDocumentTypeIdNot(String documentName, Long documentTypeId);
}
