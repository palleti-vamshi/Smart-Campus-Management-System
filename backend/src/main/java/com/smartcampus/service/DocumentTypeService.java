package com.smartcampus.service;

import com.smartcampus.dto.request.DocumentTypeRequest;
import com.smartcampus.dto.response.DocumentTypeResponse;
import com.smartcampus.entity.DocumentType;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DocumentRequestRepository;
import com.smartcampus.repository.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentRequestRepository documentRequestRepository;

    @Transactional
    public DocumentTypeResponse createDocumentType(DocumentTypeRequest request) {
        if (documentTypeRepository.existsByDocumentName(request.getDocumentName().trim())) {
            throw new ResourceConflictException("Document type already exists with name: " + request.getDocumentName());
        }

        DocumentType documentType = DocumentType.builder()
                .documentName(request.getDocumentName().trim())
                .description(request.getDescription())
                .requiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : true)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        DocumentType saved = documentTypeRepository.save(documentType);
        log.info("Document type created with ID: {} name: {}", saved.getDocumentTypeId(), saved.getDocumentName());
        return DocumentTypeResponse.from(saved);
    }

    public List<DocumentTypeResponse> getAllDocumentTypes() {
        return documentTypeRepository.findAll()
                .stream()
                .map(DocumentTypeResponse::from)
                .toList();
    }

    public List<DocumentTypeResponse> getActiveDocumentTypes() {
        return documentTypeRepository.findByIsActiveTrue()
                .stream()
                .map(DocumentTypeResponse::from)
                .toList();
    }

    public DocumentTypeResponse getDocumentType(Long documentTypeId) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with ID: " + documentTypeId));
        return DocumentTypeResponse.from(documentType);
    }

    @Transactional
    public DocumentTypeResponse updateDocumentType(Long documentTypeId, DocumentTypeRequest request) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with ID: " + documentTypeId));

        String trimmedName = request.getDocumentName().trim();
        if (documentTypeRepository.existsByDocumentNameAndDocumentTypeIdNot(trimmedName, documentTypeId)) {
            throw new ResourceConflictException("Another document type already exists with name: " + trimmedName);
        }

        documentType.setDocumentName(trimmedName);
        documentType.setDescription(request.getDescription());
        if (request.getRequiresApproval() != null) {
            documentType.setRequiresApproval(request.getRequiresApproval());
        }
        if (request.getIsActive() != null) {
            documentType.setIsActive(request.getIsActive());
        }

        DocumentType updated = documentTypeRepository.save(documentType);
        log.info("Document type ID {} updated", documentTypeId);
        return DocumentTypeResponse.from(updated);
    }

    @Transactional
    public void deleteDocumentType(Long documentTypeId) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found with ID: " + documentTypeId));

        if (documentRequestRepository.existsByDocumentType_DocumentTypeId(documentTypeId)) {
            throw new ResourceConflictException("Cannot delete document type because it is referenced by existing document requests");
        }

        documentTypeRepository.delete(documentType);
        log.info("Document type ID {} deleted", documentTypeId);
    }
}
