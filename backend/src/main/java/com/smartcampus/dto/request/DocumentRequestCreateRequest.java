package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for student document requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequestCreateRequest {

    @NotNull(message = "Document type ID is required")
    private Long documentTypeId;

    @NotBlank(message = "Purpose is required")
    @Size(max = 255, message = "Purpose must not exceed 255 characters")
    private String purpose;

    private String additionalDetails;
}
