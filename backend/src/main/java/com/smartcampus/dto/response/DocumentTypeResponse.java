package com.smartcampus.dto.response;

import com.smartcampus.entity.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response payload representing a DocumentType.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeResponse {

    private Long documentTypeId;
    private String documentName;
    private String description;
    private Boolean requiresApproval;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static DocumentTypeResponse from(DocumentType type) {
        if (type == null) {
            return null;
        }
        return DocumentTypeResponse.builder()
                .documentTypeId(type.getDocumentTypeId())
                .documentName(type.getDocumentName())
                .description(type.getDescription())
                .requiresApproval(type.getRequiresApproval())
                .isActive(type.getIsActive())
                .createdAt(type.getCreatedAt())
                .build();
    }
}
