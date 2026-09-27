package com.smartcampus.dto.dashboard;

import com.smartcampus.dto.response.NoticeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeSummary {
    private long totalActive;
    private long urgentCount;

    @Builder.Default
    private Map<String, Long> byCategory = new HashMap<>();

    @Builder.Default
    private List<NoticeResponse> recentNotices = new ArrayList<>();
}
