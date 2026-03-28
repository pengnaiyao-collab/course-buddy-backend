package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.MessageDTO;
import com.coursebuddy.domain.vo.MessageVO;
import com.coursebuddy.service.IMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Messages", description = "Direct messaging endpoints")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService service;

    @Operation(summary = "Send a message", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageVO> send(@Valid @RequestBody MessageDTO dto) {
        return ApiResponse.success("Message sent", service.sendMessage(dto));
    }

    @Operation(summary = "Get conversation with a user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/conversation/{otherUserId}")
    public ApiResponse<Page<MessageVO>> getConversation(
            @PathVariable Long otherUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.getConversation(otherUserId, pageable));
    }

    @Operation(summary = "List inbox messages", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/inbox")
    public ApiResponse<Page<MessageVO>> listInbox(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listInbox(pageable));
    }

    @Operation(summary = "Mark conversation as read", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/conversation/{senderId}/read")
    public ApiResponse<Map<String, Object>> markRead(@PathVariable Long senderId) {
        int count = service.markConversationRead(senderId);
        return ApiResponse.success(Map.of("markedRead", count));
    }

    @Operation(summary = "Count unread messages", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> countUnread() {
        return ApiResponse.success(Map.of("unreadCount", service.countUnread()));
    }

    @Operation(summary = "Delete a message", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteMessage(id);
        return ApiResponse.success(null);
    }
}
