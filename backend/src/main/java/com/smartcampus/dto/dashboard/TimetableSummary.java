package com.smartcampus.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSummary {
    private long totalEntries;
    private long todayClassesCount;

    @Builder.Default
    private List<TimetableClassSummary> todaySchedule = new ArrayList<>();

    @Builder.Default
    private List<TimetableClassSummary> weeklySchedule = new ArrayList<>();
}
