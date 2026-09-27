package com.smartcampus.controller;

import com.smartcampus.dto.request.DocumentTypeRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.DocumentTypeResponse;
import com.smartcampus.service.DocumentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/document-types")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DocumentTypeAdminController {

    private final DocumentTypeService documentTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentTypeResponse>> createDocumentType(
            @Valid @RequestBody DocumentTypeRequest request) {
        DocumentTypeResponse response = documentTypeService.createDocumentType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document type created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentTypeResponse>>> getAllDocumentTypes() {
        List<DocumentTypeResponse> response = documentTypeService.getAllDocumentTypes();
        return ResponseEntity.ok(ApiResponse.success("Document types retrieved successfully", response));
    }

    @GetMapping("/{documentTypeId}")
    public ResponseEntity<ApiResponse<DocumentTypeResponse>> getDocumentType(
            @PathVariable Long documentTypeId) {
        DocumentTypeResponse response = documentTypeService.getDocumentType(documentTypeId);
        return ResponseEntity.ok(ApiResponse.success("Document type retrieved successfully", response));
    }

    @PutMapping("/{documentTypeId}")
    public ResponseEntity<ApiResponse<DocumentTypeResponse>> updateDocumentType(
            @PathVariable Long documentTypeId,
            @Valid @RequestBody DocumentTypeRequest request) {
        DocumentTypeResponse response = documentTypeService.updateDocumentType(documentTypeId, request);
        return ResponseEntity.ok(ApiResponse.success("Document type updated successfully", response));
    }

    @DeleteMapping("/{documentTypeId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentType(
            @PathVariable Long documentTypeId) {
        documentTypeService.deleteDocumentType(documentTypeId);
        return ResponseEntity.ok(ApiResponse.success("Document type deleted successfully", null));
    }
}
