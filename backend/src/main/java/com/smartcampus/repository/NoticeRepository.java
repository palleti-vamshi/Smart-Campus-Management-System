package com.smartcampus.repository;

import com.smartcampus.entity.Notice;
import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            SELECT n
            FROM Notice n
            WHERE (n.targetProgram.programId = :programId
                   OR n.targetProgram IS NULL)
              AND n.publishAt <= :now
              AND (n.expiresAt IS NULL OR n.expiresAt >= :now)
              AND (:category IS NULL OR n.category = :category)
              AND (:priority IS NULL OR n.priority = :priority)
              AND (:keyword IS NULL OR (
                    LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  ))
            """)
    Page<Notice> findStudentNotices(
            @Param("programId") Long programId,
            @Param("now") LocalDateTime now,
            @Param("category") NoticeCategory category,
            @Param("priority") NoticePriority priority,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
            SELECT n
            FROM Notice n
            WHERE (:category IS NULL OR n.category = :category)
              AND (:priority IS NULL OR n.priority = :priority)
              AND (:keyword IS NULL OR (
                    LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  ))
              AND (:active IS NULL OR (
                    (:active = true AND n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now))
                    OR (:active = false AND (n.publishAt > :now OR (n.expiresAt IS NOT NULL AND n.expiresAt < :now)))
                  ))
            """)
    Page<Notice> findWithAdminFilters(
            @Param("category") NoticeCategory category,
            @Param("priority") NoticePriority priority,
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("""
            SELECT n
            FROM Notice n
            WHERE (n.targetProgram.programId = :programId
                   OR n.targetProgram IS NULL)
              AND n.publishAt <= :now
              AND (n.expiresAt IS NULL OR n.expiresAt >= :now)
            """)
    Page<Notice> findActiveNoticesForProgram(
            @Param("programId") Long programId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("""
            SELECT n
            FROM Notice n
            WHERE n.publishAt <= :now
              AND (n.expiresAt IS NULL OR n.expiresAt >= :now)
            """)
    Page<Notice> findAllActiveNotices(
            @Param("now") LocalDateTime now,
            Pageable pageable);

    Page<Notice> findByCategory(
            NoticeCategory category,
            Pageable pageable);

    Page<Notice> findByPriority(
            NoticePriority priority,
            Pageable pageable);

    @Query("""
            SELECT n
            FROM Notice n
            WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Notice> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now)")
    long countActiveNotices(@Param("now") LocalDateTime now);

    @Query("SELECT n.category, COUNT(n) FROM Notice n WHERE n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now) GROUP BY n.category")
    List<Object[]> countActiveNoticesByCategory(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now) AND n.priority = com.smartcampus.entity.enums.NoticePriority.URGENT")
    long countActiveUrgentNotices(@Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notice n WHERE (n.targetProgram.programId = :programId OR n.targetProgram IS NULL) AND n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now) ORDER BY n.publishAt DESC")
    List<Notice> findActiveNoticesForProgramAndDept(@Param("programId") Long programId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notice n WHERE (n.targetProgram.programId = :programId OR n.targetProgram IS NULL) AND n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now) AND n.priority = com.smartcampus.entity.enums.NoticePriority.URGENT")
    long countActiveUrgentNoticesForProgramAndDept(@Param("programId") Long programId, @Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notice n WHERE n.targetProgram IS NULL AND n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now) ORDER BY n.publishAt DESC")
    List<Notice> findActiveDepartmentWideNotices(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.targetProgram IS NULL AND n.publishAt <= :now AND (n.expiresAt IS NULL OR n.expiresAt >= :now) AND n.priority = com.smartcampus.entity.enums.NoticePriority.URGENT")
    long countActiveUrgentDepartmentWideNotices(@Param("now") LocalDateTime now);
}

