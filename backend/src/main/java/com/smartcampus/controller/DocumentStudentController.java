package com.smartcampus.controller;

import com.smartcampus.dto.request.DocumentRequestCreateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.DocumentRequestHistoryResponse;
import com.smartcampus.dto.response.DocumentRequestResponse;
import com.smartcampus.dto.response.DocumentTypeResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.DocumentRequestService;
import com.smartcampus.service.DocumentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class DocumentStudentController {

    private final DocumentTypeService documentTypeService;
    private final DocumentRequestService documentRequestService;

    @GetMapping("/document-types")
    public ResponseEntity<ApiResponse<List<DocumentTypeResponse>>> getActiveDocumentTypes() {
        List<DocumentTypeResponse> response = documentTypeService.getActiveDocumentTypes();
        return ResponseEntity.ok(ApiResponse.success("Active document types retrieved successfully", response));
    }

    @PostMapping("/document-requests")
    public ResponseEntity<ApiResponse<DocumentRequestResponse>> createDocumentRequest(
            @Valid @RequestBody DocumentRequestCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DocumentRequestResponse response = documentRequestService.createStudentRequest(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document request submitted successfully", response));
    }

    @GetMapping("/document-requests")
    public ResponseEntity<ApiResponse<PageResponse<DocumentRequestResponse>>> getMyDocumentRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "requestId", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<DocumentRequestResponse> response = PageResponse.from(
                documentRequestService.getStudentRequests(userDetails.getUserId(), pageable)
        );
        return ResponseEntity.ok(ApiResponse.success("Student document requests retrieved successfully", response));
    }

    @GetMapping("/document-requests/{requestId}")
    public ResponseEntity<ApiResponse<DocumentRequestResponse>> getMyDocumentRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DocumentRequestResponse response = documentRequestService.getStudentRequest(requestId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Document request retrieved successfully", response));
    }

    @GetMapping("/document-requests/{requestId}/history")
    public ResponseEntity<ApiResponse<List<DocumentRequestHistoryResponse>>> getMyDocumentRequestHistory(
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<DocumentRequestHistoryResponse> response = documentRequestService.getStudentRequestHistory(requestId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Document request history retrieved successfully", response));
    }

    @GetMapping("/document-requests/{requestId}/download")
    public ResponseEntity<Resource> downloadMyDocument(
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Resource file = documentRequestService.downloadStudentDocument(requestId, userDetails.getUserId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
