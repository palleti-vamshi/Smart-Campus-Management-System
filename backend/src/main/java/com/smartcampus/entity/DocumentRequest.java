package com.smartcampus.entity;

import com.smartcampus.entity.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a student's request for a digital document/certificate.
 *
 * Status lifecycle: SUBMITTED → UNDER_REVIEW → APPROVED → ISSUED
 *                                            → REJECTED (terminal)
 *
 * reviewed_by references users (not faculty) so that ADMIN users
 * can also review document requests.
 *
 * Maps to the 'document_requests' table.
 */
@Entity
@Table(name = "document_requests", indexes = {
        @Index(name = "idx_docreq_student", columnList = "student_id"),
        @Index(name = "idx_docreq_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "request_number", length = 50, unique = true, nullable = false)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(name = "purpose", length = 255, nullable = false)
    private String purpose;

    @Column(name = "additional_details", columnDefinition = "TEXT")
    private String additionalDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private DocumentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "document_path", length = 500)
    private String documentPath;

    @Column(name = "verification_code", length = 100, unique = true)
    private String verificationCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "documentRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<DocumentRequestHistory> history = new java.util.ArrayList<>();
}

