package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.DocumentVerificationResponse;
import com.smartcampus.service.DocumentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint for verifying digital documents and certificates.
 * Requires no authentication.
 */
@RestController
@RequestMapping("/api/public/documents")
@RequiredArgsConstructor
public class DocumentPublicController {

    private final DocumentRequestService documentRequestService;

    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<ApiResponse<DocumentVerificationResponse>> verifyDocument(
            @PathVariable String verificationCode) {
        DocumentVerificationResponse response = documentRequestService.verifyDocument(verificationCode);
        return ResponseEntity.ok(ApiResponse.success("Document verified successfully", response));
    }
}
