package com.smartcampus.repository;

import com.smartcampus.entity.Notice;
import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Notice entity.
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByTargetProgram_ProgramIdOrTargetProgramIsNull(Long programId);

    List<Notice> findByTargetProgramIsNull();

    List<Notice> findByCategory(NoticeCategory category);

    List<Notice> findByPriority(NoticePriority priority);
}
