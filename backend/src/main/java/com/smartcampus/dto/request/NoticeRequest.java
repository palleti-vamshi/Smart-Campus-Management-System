package com.smartcampus.dto.request;

import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class NoticeRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Category is required")
    private NoticeCategory category;

    @NotNull(message = "Priority is required")
    private NoticePriority priority;

    /**
     * Null means department-wide notice.
     * Non-null means notice is targeted to a specific program.
     */
    private Long targetProgramId;

    @NotNull(message = "Publish time is required")
    private LocalDateTime publishAt;

    private LocalDateTime expiresAt;
}
