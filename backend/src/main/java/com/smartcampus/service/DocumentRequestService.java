package com.smartcampus.service;

import com.smartcampus.dto.request.DocumentRequestCreateRequest;
import com.smartcampus.dto.request.DocumentStatusUpdateRequest;
import com.smartcampus.dto.response.DocumentRequestHistoryResponse;
import com.smartcampus.dto.response.DocumentRequestResponse;
import com.smartcampus.dto.response.DocumentVerificationResponse;
import com.smartcampus.entity.DocumentRequest;
import com.smartcampus.entity.DocumentRequestHistory;
import com.smartcampus.entity.DocumentType;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.entity.enums.DocumentStatus;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DocumentRequestHistoryRepository;
import com.smartcampus.repository.DocumentRequestRepository;
import com.smartcampus.repository.DocumentTypeRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentRequestService {

    private final DocumentRequestRepository documentRequestRepository;
    private final DocumentRequestHistoryRepository documentRequestHistoryRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CertificateGeneratorService certificateGeneratorService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // exclude easily confusable chars 0, 1, O, I

    /**
     * Creates a new document request for the authenticated student.
     */
    @Transactional
    public DocumentRequestResponse createStudentRequest(DocumentRequestCreateRequest request, Long authenticatedUserId) {
        Student student = studentRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user does not have a student profile"));

        DocumentType documentType = documentTypeRepository.findById(request.getDocumentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with ID: " + request.getDocumentTypeId()));

        if (!Boolean.TRUE.equals(documentType.getIsActive())) {
            throw new InvalidOperationException("Document type is inactive and cannot be requested");
        }

        String requestNumber = generateUniqueRequestNumber();
        LocalDateTime now = LocalDateTime.now();

        DocumentRequest documentRequest = DocumentRequest.builder()
                .requestNumber(requestNumber)
                .student(student)
                .documentType(documentType)
                .purpose(request.getPurpose().trim())
                .additionalDetails(request.getAdditionalDetails() != null ? request.getAdditionalDetails().trim() : null)
                .status(DocumentStatus.SUBMITTED)
                .submittedAt(now)
                .build();

        DocumentRequest savedRequest = documentRequestRepository.save(documentRequest);

        // Initial audit history row
        DocumentRequestHistory history = DocumentRequestHistory.builder()
                .documentRequest(savedRequest)
                .oldStatus(null)
                .newStatus(DocumentStatus.SUBMITTED.name())
                .changedBy(student.getUser())
                .remarks("Document request submitted")
                .changedAt(now)
                .build();

        documentRequestHistoryRepository.save(history);
        log.info("Document request {} submitted by student {}", requestNumber, student.getRollNumber());

        return DocumentRequestResponse.from(savedRequest);
    }

    /**
     * Lists all requests created by the authenticated student.
     */
    public Page<DocumentRequestResponse> getStudentRequests(Long authenticatedUserId, Pageable pageable) {
        Student student = studentRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user does not have a student profile"));

        return documentRequestRepository.findByStudent_StudentId(student.getStudentId(), pageable)
                .map(DocumentRequestResponse::from);
    }

    /**
     * Retrieves a single request belonging to the authenticated student.
     */
    public DocumentRequestResponse getStudentRequest(Long requestId, Long authenticatedUserId) {
        Student student = studentRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user does not have a student profile"));

        DocumentRequest documentRequest = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Document request not found with ID: " + requestId));

        if (!documentRequest.getStudent().getStudentId().equals(student.getStudentId())) {
            log.warn("Student {} attempted to access document request {} belonging to student {}",
                    student.getStudentId(), requestId, documentRequest.getStudent().getStudentId());
            throw new AccessDeniedException("You are not authorized to view this document request");
        }

        return DocumentRequestResponse.from(documentRequest);
    }

    /**
     * Retrieves the audit history for a request belonging to the authenticated student.
     */
    public List<DocumentRequestHistoryResponse> getStudentRequestHistory(Long requestId, Long authenticatedUserId) {
        Student student = studentRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user does not have a student profile"));

        DocumentRequest documentRequest = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Document request not found with ID: " + requestId));

        if (!documentRequest.getStudent().getStudentId().equals(student.getStudentId())) {
            log.warn("Student {} attempted to access history for document request {} belonging to student {}",
                    student.getStudentId(), requestId, documentRequest.getStudent().getStudentId());
            throw new AccessDeniedException("You are not authorized to view this document request history");
        }

        return documentRequestHistoryRepository.findByDocumentRequest_RequestIdOrderByChangedAtAsc(requestId)
                .stream()
                .map(DocumentRequestHistoryResponse::from)
                .toList();
    }

    /**
     * Downloads an issued document for the authenticated student.
     */
    public Resource downloadStudentDocument(Long requestId, Long authenticatedUserId) {
        Student student = studentRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user does not have a student profile"));

        DocumentRequest documentRequest = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Document request not found with ID: " + requestId));

        if (!documentRequest.getStudent().getStudentId().equals(student.getStudentId())) {
            log.warn("Student {} attempted to download document request {} belonging to student {}",
                    student.getStudentId(), requestId, documentRequest.getStudent().getStudentId());
            throw new AccessDeniedException("You are not authorized to download this document");
        }

        if (documentRequest.getStatus() != DocumentStatus.ISSUED) {
            throw new InvalidOperationException("Document has not been issued yet and cannot be downloaded");
        }

        return certificateGeneratorService.loadAsResource(documentRequest.getDocumentPath());
    }

    /**
     * Admin: Retrieves paginated and filtered document requests.
     */
    public Page<DocumentRequestResponse> getAdminRequests(DocumentStatus status, Long documentTypeId, String search, Pageable pageable) {
        return documentRequestRepository.findWithAdminFilters(status, documentTypeId, search, pageable)
                .map(DocumentRequestResponse::from);
    }

    /**
     * Admin: Retrieves a single document request by ID.
     */
    public DocumentRequestResponse getAdminRequest(Long requestId) {
        DocumentRequest documentRequest = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Document request not found with ID: " + requestId));
        return DocumentRequestResponse.from(documentRequest);
    }

    /**
     * Admin: Retrieves the audit history for any request.
     */
    public List<DocumentRequestHistoryResponse> getAdminRequestHistory(Long requestId) {
        if (!documentRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Document request not found with ID: " + requestId);
        }

        return documentRequestHistoryRepository.findByDocumentRequest_RequestIdOrderByChangedAtAsc(requestId)
                .stream()
                .map(DocumentRequestHistoryResponse::from)
                .toList();
    }

    /**
     * Admin: Downloads any issued document.
     */
    public Resource downloadAdminDocument(Long requestId) {
        DocumentRequest documentRequest = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Document request not found with ID: " + requestId));

        if (documentRequest.getStatus() != DocumentStatus.ISSUED) {
            throw new InvalidOperationException("Document has not been issued yet and cannot be downloaded");
        }

        return certificateGeneratorService.loadAsResource(documentRequest.getDocumentPath());
    }

    /**
     * Admin: Updates request status according to the strict state machine workflow.
     */
    @Transactional
    public DocumentRequestResponse updateRequestStatus(Long requestId, DocumentStatusUpdateRequest updateRequest, Long reviewerUserId) {
        DocumentRequest documentRequest = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Document request not found with ID: " + requestId));

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer user not found with ID: " + reviewerUserId));

        DocumentStatus currentStatus = documentRequest.getStatus();
        DocumentStatus targetStatus = updateRequest.getStatus();

        validateStatusTransition(currentStatus, targetStatus);

        LocalDateTime now = LocalDateTime.now();

        if (targetStatus == DocumentStatus.UNDER_REVIEW) {
            documentRequest.setReviewedBy(reviewer);
            documentRequest.setReviewedAt(now);
        } else if (targetStatus == DocumentStatus.APPROVED) {
            documentRequest.setReviewedBy(reviewer);
            documentRequest.setReviewedAt(now);
        } else if (targetStatus == DocumentStatus.REJECTED) {
            documentRequest.setReviewedBy(reviewer);
            documentRequest.setReviewedAt(now);
        } else if (targetStatus == DocumentStatus.ISSUED) {
            if (documentRequest.getReviewedBy() == null) {
                documentRequest.setReviewedBy(reviewer);
            }
            documentRequest.setIssuedAt(now);

            // Generate unique verification code
            String verificationCode = generateUniqueVerificationCode();
            documentRequest.setVerificationCode(verificationCode);

            // Generate certificate document and set path
            String documentPath = certificateGeneratorService.generateCertificate(documentRequest);
            documentRequest.setDocumentPath(documentPath);
        }

        documentRequest.setStatus(targetStatus);
        DocumentRequest saved = documentRequestRepository.save(documentRequest);

        // Append to audit history
        String remarks = updateRequest.getRemarks();
        if (remarks == null || remarks.isBlank()) {
            remarks = "Status updated from " + currentStatus + " to " + targetStatus;
        }

        DocumentRequestHistory history = DocumentRequestHistory.builder()
                .documentRequest(saved)
                .oldStatus(currentStatus.name())
                .newStatus(targetStatus.name())
                .changedBy(reviewer)
                .remarks(remarks.trim())
                .changedAt(now)
                .build();

        documentRequestHistoryRepository.save(history);
        log.info("Document request {} transitioned from {} to {} by {}",
                saved.getRequestNumber(), currentStatus, targetStatus, reviewer.getUsername());

        return DocumentRequestResponse.from(saved);
    }

    /**
     * Public: Validates and verifies an issued document by its verification code.
     */
    public DocumentVerificationResponse verifyDocument(String verificationCode) {
        if (verificationCode == null || verificationCode.isBlank()) {
            throw new ResourceNotFoundException("Verification code must not be empty");
        }

        DocumentRequest request = documentRequestRepository.findByVerificationCode(verificationCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No document found with verification code: " + verificationCode));

        if (request.getStatus() != DocumentStatus.ISSUED) {
            throw new InvalidOperationException("Document is not in ISSUED status and cannot be verified");
        }

        String studentName = request.getStudent() != null
                ? (request.getStudent().getFirstName() + " " + (request.getStudent().getLastName() != null ? request.getStudent().getLastName() : "")).trim()
                : "N/A";
        String programName = request.getStudent() != null && request.getStudent().getProgram() != null
                ? request.getStudent().getProgram().getProgramName()
                : "N/A";

        return DocumentVerificationResponse.builder()
                .valid(true)
                .requestNumber(request.getRequestNumber())
                .documentType(request.getDocumentType() != null ? request.getDocumentType().getDocumentName() : "N/A")
                .studentName(studentName)
                .program(programName)
                .issueDate(request.getIssuedAt())
                .status(request.getStatus().name())
                .build();
    }

    /**
     * Validates allowed status transitions according to the state machine:
     * SUBMITTED → UNDER_REVIEW
     * SUBMITTED → REJECTED
     * UNDER_REVIEW → APPROVED
     * UNDER_REVIEW → REJECTED
     * APPROVED → ISSUED
     * ISSUED and REJECTED are terminal.
     */
    private void validateStatusTransition(DocumentStatus current, DocumentStatus target) {
        if (current == target) {
            throw new InvalidOperationException("Request is already in status " + target);
        }

        if (current == DocumentStatus.ISSUED || current == DocumentStatus.REJECTED) {
            throw new InvalidOperationException("Cannot change status from terminal state: " + current);
        }

        boolean allowed = false;
        if (current == DocumentStatus.SUBMITTED) {
            allowed = (target == DocumentStatus.UNDER_REVIEW || target == DocumentStatus.REJECTED);
        } else if (current == DocumentStatus.UNDER_REVIEW) {
            allowed = (target == DocumentStatus.APPROVED || target == DocumentStatus.REJECTED);
        } else if (current == DocumentStatus.APPROVED) {
            allowed = (target == DocumentStatus.ISSUED);
        }

        if (!allowed) {
            throw new InvalidOperationException("Invalid status transition from " + current + " to " + target);
        }
    }

    /**
     * Generates a unique, deterministic-style request number: DOC-YYYY-XXXXXX.
     */
    private String generateUniqueRequestNumber() {
        int year = LocalDate.now().getYear();
        for (int i = 0; i < 50; i++) {
            int randomNum = 100000 + RANDOM.nextInt(900000);
            String candidate = String.format("DOC-%d-%06d", year, randomNum);
            if (!documentRequestRepository.existsByRequestNumber(candidate)) {
                return candidate;
            }
        }
        // Fallback using timestamp
        return String.format("DOC-%d-%d", year, System.currentTimeMillis() % 1000000);
    }

    /**
     * Generates a unique verification code: SCMS-YYYY-XXXXXX.
     */
    private String generateUniqueVerificationCode() {
        int year = LocalDate.now().getYear();
        for (int attempt = 0; attempt < 50; attempt++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
            }
            String candidate = String.format("SCMS-%d-%s", year, sb.toString());
            if (!documentRequestRepository.existsByVerificationCode(candidate)) {
                return candidate;
            }
        }
        return String.format("SCMS-%d-%X", year, System.currentTimeMillis());
    }
}
