package com.smartcampus.controller;

import com.smartcampus.dto.request.NoticeRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.NoticeResponse;
import com.smartcampus.dto.response.PageResponse;
import com.smartcampus.entity.enums.NoticeCategory;
import com.smartcampus.entity.enums.NoticePriority;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faculty/notices")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyNoticeController {

    private final NoticeService noticeService;

    public FacultyNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @Valid @RequestBody NoticeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NoticeResponse response = noticeService.createNotice(
                request,
                userDetails.getUserId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Notice created successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NoticeResponse>>> getNotices(
            @RequestParam(required = false) NoticeCategory category,
            @RequestParam(required = false) NoticePriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(
                    size = 20,
                    sort = "noticeId",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        PageResponse<NoticeResponse> response =
                PageResponse.from(noticeService.getNoticesForAdmin(category, priority, keyword, active, pageable));

        return ResponseEntity.ok(
                ApiResponse.success("Notices retrieved successfully", response)
        );
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
            @PathVariable Long noticeId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notice retrieved successfully",
                        noticeService.getNotice(noticeId)
                )
        );
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NoticeResponse response = noticeService.updateNotice(
                noticeId,
                request,
                userDetails.getUserId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Notice updated successfully", response)
        );
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        noticeService.deleteNotice(
                noticeId,
                userDetails.getUserId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Notice deleted successfully", null)
        );
    }
}
