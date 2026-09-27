package com.smartcampus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Public document verification response payload.
 * Exposes only non-sensitive verification details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVerificationResponse {

    private boolean valid;
    private String requestNumber;
    private String documentType;
    private String studentName;
    private String program;
    private LocalDateTime issueDate;
    private String status;
}
