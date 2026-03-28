package com.coursebuddy.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequestDTO {

    /** 对话 ID（继续已有对话时传入，不传则新建） */
    private Long conversationId;

    /** 对话标题（新建对话时可选） */
    private String title;

    /** 发送的消息内容 */
    @NotEmpty(message = "消息内容不能为空")
    private String message;

    /** 是否包含历史上下文 */
    private boolean includeHistory = true;
}
