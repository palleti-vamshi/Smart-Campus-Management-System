package com.smartcampus.entity;

import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a department notice.
 *
 * A notice may target a specific program (target_program_id) or be
 * department-wide (target_program_id = null).
 *
 * published_by references users (not faculty) so that ADMIN users
 * can also publish notices.
 *
 * Maps to the 'notices' table.
 */
@Entity
@Table(name = "notices", indexes = {
        @Index(name = "idx_notices_target_program", columnList = "target_program_id"),
        @Index(name = "idx_notices_publish_at", columnList = "publish_at"),
        @Index(name = "idx_notices_published_by", columnList = "published_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private NoticeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20, nullable = false)
    private NoticePriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_program_id")
    private Program targetProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by", nullable = false)
    private User publishedBy;

    @Column(name = "publish_at", nullable = false)
    private LocalDateTime publishAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
