package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.ChatRequestDTO;
import com.coursebuddy.domain.vo.AiUsageStatsVO;
import com.coursebuddy.domain.vo.ChatMessageVO;
import com.coursebuddy.domain.vo.ChatResponseVO;
import com.coursebuddy.domain.vo.ConversationVO;
import com.coursebuddy.service.IXunFeiAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "XunFei AI Chat", description = "讯飞星火大模型对话接口")
@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class XunFeiController {

    private final IXunFeiAiService xunFeiAiService;

    @Operation(summary = "同步对话", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/chat")
    public ApiResponse<ChatResponseVO> chat(@Valid @RequestBody ChatRequestDTO dto) {
        return ApiResponse.success(xunFeiAiService.chat(dto));
    }

    @Operation(summary = "流式对话（SSE）", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequestDTO dto) {
        return xunFeiAiService.chatStream(dto);
    }

    @Operation(summary = "获取对话列表", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/conversations")
    public ApiResponse<Page<ConversationVO>> listConversations(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(xunFeiAiService.listConversations(pageable));
    }

    @Operation(summary = "获取对话消息历史", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<List<ChatMessageVO>> getMessages(@PathVariable Long id) {
        return ApiResponse.success(xunFeiAiService.getConversationMessages(id));
    }

    @Operation(summary = "归档对话", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/conversations/{id}/archive")
    public ApiResponse<ConversationVO> archiveConversation(@PathVariable Long id) {
        return ApiResponse.success(xunFeiAiService.archiveConversation(id));
    }

    @Operation(summary = "删除对话", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(@PathVariable Long id) {
        xunFeiAiService.deleteConversation(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取 AI 使用统计", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/usage-stats")
    public ApiResponse<Page<AiUsageStatsVO>> getUsageStats(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(xunFeiAiService.getUsageStats(pageable));
    }
}
