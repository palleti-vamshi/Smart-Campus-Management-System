package com.smartcampus.service;

import com.smartcampus.dto.request.NoticeRequest;
import com.smartcampus.dto.response.NoticeResponse;
import com.smartcampus.entity.Notice;
import com.smartcampus.entity.Program;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import com.smartcampus.entity.enums.Role;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.NoticeRepository;
import com.smartcampus.repository.ProgramRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final ProgramRepository programRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public NoticeService(
            NoticeRepository noticeRepository,
            ProgramRepository programRepository,
            StudentRepository studentRepository,
            UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.programRepository = programRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NoticeResponse createNotice(NoticeRequest request, Long publisherUserId) {
        validateDates(request);

        User publisher = userRepository.findById(publisherUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Publisher not found with id: " + publisherUserId));

        Program targetProgram = getTargetProgram(request.getTargetProgramId());

        Notice notice = new Notice();
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setCategory(request.getCategory());
        notice.setPriority(request.getPriority());
        notice.setTargetProgram(targetProgram);
        notice.setPublishedBy(publisher);
        notice.setPublishAt(request.getPublishAt());
        notice.setExpiresAt(request.getExpiresAt());

        Notice saved = noticeRepository.save(notice);

        log.info("Notice created: noticeId={}, publisherUserId={}",
                saved.getNoticeId(), publisherUserId);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = getNoticeEntity(noticeId);
        return toResponse(notice);
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNoticeForStudent(Long noticeId, Long studentUserId) {
        Student student = studentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for user: " + studentUserId));

        Notice notice = getNoticeEntity(noticeId);

        LocalDateTime now = LocalDateTime.now();
        boolean active = !notice.getPublishAt().isAfter(now)
                && (notice.getExpiresAt() == null || !notice.getExpiresAt().isBefore(now));

        boolean relevant = notice.getTargetProgram() == null
                || notice.getTargetProgram().getProgramId().equals(student.getProgram().getProgramId());

        if (!active || !relevant) {
            log.warn("Student {} attempted to access inactive or non-relevant notice {}",
                    studentUserId, noticeId);
            throw new AccessDeniedException("You do not have permission to view this notice");
        }

        return toResponse(notice);
    }

    @Transactional
    public NoticeResponse updateNotice(
            Long noticeId,
            NoticeRequest request,
            Long publisherUserId) {

        validateDates(request);

        Notice notice = getNoticeEntity(noticeId);

        User user = userRepository.findById(publisherUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + publisherUserId));

        if (user.getRole() != Role.ADMIN && !notice.getPublishedBy().getUserId().equals(publisherUserId)) {
            log.warn("User {} attempted to update notice {} owned by user {}",
                    publisherUserId, noticeId, notice.getPublishedBy().getUserId());
            throw new AccessDeniedException("You are not authorized to update another user's notice");
        }

        Program targetProgram = getTargetProgram(request.getTargetProgramId());

        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setCategory(request.getCategory());
        notice.setPriority(request.getPriority());
        notice.setTargetProgram(targetProgram);
        notice.setPublishAt(request.getPublishAt());
        notice.setExpiresAt(request.getExpiresAt());

        Notice updated = noticeRepository.save(notice);

        log.info("Notice updated: noticeId={}, publisherUserId={}",
                noticeId, publisherUserId);

        return toResponse(updated);
    }

    @Transactional
    public void deleteNotice(Long noticeId, Long publisherUserId) {
        Notice notice = getNoticeEntity(noticeId);

        User user = userRepository.findById(publisherUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + publisherUserId));

        if (user.getRole() != Role.ADMIN && !notice.getPublishedBy().getUserId().equals(publisherUserId)) {
            log.warn("User {} attempted to delete notice {} owned by user {}",
                    publisherUserId, noticeId, notice.getPublishedBy().getUserId());
            throw new AccessDeniedException("You are not authorized to delete another user's notice");
        }

        noticeRepository.delete(notice);

        log.info("Notice deleted: noticeId={}, publisherUserId={}",
                noticeId, publisherUserId);
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getAllNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getNoticesForAdmin(
            NoticeCategory category,
            NoticePriority priority,
            String keyword,
            Boolean active,
            Pageable pageable) {
        return noticeRepository.findWithAdminFilters(
                category, priority, keyword, active, LocalDateTime.now(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getActiveNotices(Pageable pageable) {
        return noticeRepository.findAllActiveNotices(
                        LocalDateTime.now(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getNoticesForStudent(
            Long studentUserId,
            Pageable pageable) {
        return getNoticesForStudent(studentUserId, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getNoticesForStudent(
            Long studentUserId,
            NoticeCategory category,
            NoticePriority priority,
            String keyword,
            Pageable pageable) {

        Student student = studentRepository.findByUser_UserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for user: " + studentUserId));

        Long programId = student.getProgram().getProgramId();

        return noticeRepository.findStudentNotices(
                        programId,
                        LocalDateTime.now(),
                        category,
                        priority,
                        keyword,
                        pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> searchNotices(
            String keyword,
            Pageable pageable) {

        return noticeRepository.searchByKeyword(keyword, pageable)
                .map(this::toResponse);
    }

    private Notice getNoticeEntity(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notice not found with id: " + noticeId));
    }

    private Program getTargetProgram(Long programId) {
        if (programId == null) {
            return null;
        }

        return programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Target program not found with id: " + programId));
    }

    private void validateDates(NoticeRequest request) {
        if (request.getExpiresAt() != null
                && request.getExpiresAt().isBefore(request.getPublishAt())) {
            throw new InvalidOperationException(
                    "Expiry time cannot be before publish time");
        }
    }

    private NoticeResponse toResponse(Notice notice) {
        Long targetProgramId = null;
        String targetProgramCode = null;
        String targetProgramName = null;

        if (notice.getTargetProgram() != null) {
            targetProgramId = notice.getTargetProgram().getProgramId();
            targetProgramCode = notice.getTargetProgram().getProgramCode();
            targetProgramName = notice.getTargetProgram().getProgramName();
        }

        User publisher = notice.getPublishedBy();

        // User does not expose Faculty/Student profile relationships.
        // Use the publisher username as the display name.
        String publisherName = publisher.getUsername();

        LocalDateTime now = LocalDateTime.now();

        boolean active = !notice.getPublishAt().isAfter(now)
                && (notice.getExpiresAt() == null
                || !notice.getExpiresAt().isBefore(now));

        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCategory(),
                notice.getPriority(),
                targetProgramId,
                targetProgramCode,
                targetProgramName,
                publisher.getUserId(),
                publisherName,
                notice.getPublishAt(),
                notice.getExpiresAt(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                active
        );
    }
}
