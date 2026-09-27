package com.smartcampus.repository;

import com.smartcampus.entity.DocumentRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the DocumentRequestHistory entity.
 */
@Repository
public interface DocumentRequestHistoryRepository extends JpaRepository<DocumentRequestHistory, Long> {

    List<DocumentRequestHistory> findByDocumentRequest_RequestIdOrderByChangedAtAsc(Long requestId);
}
