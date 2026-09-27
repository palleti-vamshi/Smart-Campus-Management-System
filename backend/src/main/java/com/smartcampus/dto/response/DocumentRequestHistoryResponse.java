package com.smartcampus.dto.response;

import com.smartcampus.entity.DocumentRequestHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response payload representing a history/audit item for a document request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequestHistoryResponse {

    private Long historyId;
    private Long requestId;
    private String oldStatus;
    private String newStatus;
    private Long changedByUserId;
    private String changedByUsername;
    private String remarks;
    private LocalDateTime changedAt;

    public static DocumentRequestHistoryResponse from(DocumentRequestHistory history) {
        if (history == null) {
            return null;
        }

        Long reqId = null;
        if (history.getDocumentRequest() != null) {
            reqId = history.getDocumentRequest().getRequestId();
        }

        Long changedByUserId = null;
        String changedByUsername = null;
        if (history.getChangedBy() != null) {
            changedByUserId = history.getChangedBy().getUserId();
            changedByUsername = history.getChangedBy().getUsername();
        }

        return DocumentRequestHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .requestId(reqId)
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedByUserId(changedByUserId)
                .changedByUsername(changedByUsername)
                .remarks(history.getRemarks())
                .changedAt(history.getChangedAt())
                .build();
    }
}
