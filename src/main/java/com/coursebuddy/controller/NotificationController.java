package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.vo.NotificationVO;
import com.coursebuddy.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Notifications", description = "Notification management endpoints")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService service;

    @Operation(summary = "List my notifications", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<NotificationVO>> list(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyNotifications(isRead, type, pageable));
    }

    @Operation(summary = "Get notification by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<NotificationVO> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Mark a notification as read", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/read")
    public ApiResponse<NotificationVO> markRead(@PathVariable Long id) {
        return ApiResponse.success(service.markRead(id));
    }

    @Operation(summary = "Mark all notifications as read", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/read-all")
    public ApiResponse<Map<String, Object>> markAllRead() {
        int count = service.markAllRead();
        return ApiResponse.success(Map.of("markedRead", count));
    }

    @Operation(summary = "Delete a notification", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Count unread notifications", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> countUnread() {
        return ApiResponse.success(Map.of("unreadCount", service.countUnread()));
    }
}
