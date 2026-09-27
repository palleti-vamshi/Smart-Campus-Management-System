package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.NoticeResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.NoticeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/notices")
@PreAuthorize("hasRole('STUDENT')")
public class StudentNoticeController {

    private final NoticeService noticeService;

    public StudentNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NoticeResponse>>> getMyNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) NoticeCategory category,
            @RequestParam(required = false) NoticePriority priority,
            @RequestParam(required = false) String keyword,
            @PageableDefault(
                    size = 20,
                    sort = "publishAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        PageResponse<NoticeResponse> response =
                PageResponse.from(
                        noticeService.getNoticesForStudent(
                                userDetails.getUserId(),
                                category,
                                priority,
                                keyword,
                                pageable
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student notices retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notice retrieved successfully",
                        noticeService.getNoticeForStudent(noticeId, userDetails.getUserId())
                )
        );
    }
}
