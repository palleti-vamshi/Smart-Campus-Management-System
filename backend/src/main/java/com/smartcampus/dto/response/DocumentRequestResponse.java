package com.smartcampus.dto.response;

import com.smartcampus.entity.DocumentRequest;
import com.smartcampus.entity.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response payload representing a digital document request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequestResponse {

    private Long requestId;
    private String requestNumber;
    private Long studentId;
    private String studentRollNumber;
    private String studentName;
    private Long programId;
    private String programName;
    private Long documentTypeId;
    private String documentTypeName;
    private String purpose;
    private String additionalDetails;
    private DocumentStatus status;
    private Long reviewedByUserId;
    private String reviewedByUsername;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime issuedAt;
    private String documentPath;
    private String verificationCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DocumentRequestResponse from(DocumentRequest request) {
        if (request == null) {
            return null;
        }

        String studentName = null;
        String rollNumber = null;
        Long studentId = null;
        Long programId = null;
        String programName = null;

        if (request.getStudent() != null) {
            studentId = request.getStudent().getStudentId();
            rollNumber = request.getStudent().getRollNumber();
            studentName = request.getStudent().getFirstName()
                    + (request.getStudent().getLastName() != null ? " " + request.getStudent().getLastName() : "");
            if (request.getStudent().getProgram() != null) {
                programId = request.getStudent().getProgram().getProgramId();
                programName = request.getStudent().getProgram().getProgramName();
            }
        }

        Long documentTypeId = null;
        String documentTypeName = null;
        if (request.getDocumentType() != null) {
            documentTypeId = request.getDocumentType().getDocumentTypeId();
            documentTypeName = request.getDocumentType().getDocumentName();
        }

        Long reviewedByUserId = null;
        String reviewedByUsername = null;
        if (request.getReviewedBy() != null) {
            reviewedByUserId = request.getReviewedBy().getUserId();
            reviewedByUsername = request.getReviewedBy().getUsername();
        }

        return DocumentRequestResponse.builder()
                .requestId(request.getRequestId())
                .requestNumber(request.getRequestNumber())
                .studentId(studentId)
                .studentRollNumber(rollNumber)
                .studentName(studentName)
                .programId(programId)
                .programName(programName)
                .documentTypeId(documentTypeId)
                .documentTypeName(documentTypeName)
                .purpose(request.getPurpose())
                .additionalDetails(request.getAdditionalDetails())
                .status(request.getStatus())
                .reviewedByUserId(reviewedByUserId)
                .reviewedByUsername(reviewedByUsername)
                .submittedAt(request.getSubmittedAt())
                .reviewedAt(request.getReviewedAt())
                .issuedAt(request.getIssuedAt())
                .documentPath(request.getDocumentPath())
                .verificationCode(request.getVerificationCode())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
