package com.smartcampus.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSummary {
    private long totalRequests;
    private long pendingRequests; // submitted + under_review
    private long submittedRequests;
    private long underReviewRequests;
    private long approvedRequests;
    private long issuedRequests;
    private long rejectedRequests;
}
