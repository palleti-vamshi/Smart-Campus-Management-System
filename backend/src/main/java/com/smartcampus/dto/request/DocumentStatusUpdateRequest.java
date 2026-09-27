package com.smartcampus.dto.request;

import com.smartcampus.entity.enums.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for administrative document request status changes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private DocumentStatus status;

    private String remarks;
}
