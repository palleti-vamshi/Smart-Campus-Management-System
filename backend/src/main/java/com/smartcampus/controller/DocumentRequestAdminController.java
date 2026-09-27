package com.smartcampus.controller;

import com.smartcampus.dto.request.DocumentStatusUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.DocumentRequestHistoryResponse;
import com.smartcampus.dto.response.DocumentRequestResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.DocumentStatus;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.DocumentRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/document-requests")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DocumentRequestAdminController {

    private final DocumentRequestService documentRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DocumentRequestResponse>>> getAllDocumentRequests(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Long documentTypeId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "requestId", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<DocumentRequestResponse> response = PageResponse.from(
                documentRequestService.getAdminRequests(status, documentTypeId, search, pageable)
        );
        return ResponseEntity.ok(ApiResponse.success("Document requests retrieved successfully", response));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<DocumentRequestResponse>> getDocumentRequest(
            @PathVariable Long requestId) {
        DocumentRequestResponse response = documentRequestService.getAdminRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success("Document request retrieved successfully", response));
    }

    @GetMapping("/{requestId}/history")
    public ResponseEntity<ApiResponse<List<DocumentRequestHistoryResponse>>> getDocumentRequestHistory(
            @PathVariable Long requestId) {
        List<DocumentRequestHistoryResponse> response = documentRequestService.getAdminRequestHistory(requestId);
        return ResponseEntity.ok(ApiResponse.success("Document request history retrieved successfully", response));
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<ApiResponse<DocumentRequestResponse>> updateDocumentRequestStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody DocumentStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DocumentRequestResponse response = documentRequestService.updateRequestStatus(
                requestId,
                request,
                userDetails.getUserId()
        );
        return ResponseEntity.ok(ApiResponse.success("Document request status updated successfully", response));
    }

    @GetMapping("/{requestId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long requestId) {
        Resource file = documentRequestService.downloadAdminDocument(requestId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
