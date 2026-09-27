package com.smartcampus.dto.response;

import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponse {

    private Long noticeId;

    private String title;

    private String content;

    private NoticeCategory category;

    private NoticePriority priority;

    private Long targetProgramId;

    private String targetProgramCode;

    private String targetProgramName;

    private Long publishedByUserId;

    private String publishedByName;

    private LocalDateTime publishAt;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean active;
}
