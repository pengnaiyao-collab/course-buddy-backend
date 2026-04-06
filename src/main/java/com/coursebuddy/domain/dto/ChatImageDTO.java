package com.coursebuddy.domain.dto;

import lombok.Data;

/**
 * 聊天传输对象
 */
@Data
public class ChatImageDTO {

    private String base64Data;

    private String mimeType;

    private String fileName;
}
