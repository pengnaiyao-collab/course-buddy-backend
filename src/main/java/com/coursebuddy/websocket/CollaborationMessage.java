package com.coursebuddy.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationMessage {

    private String type;       // JOIN, LEAVE, EDIT, CURSOR, CHAT
    private String sessionId;
    private String userId;
    private String content;
    private String payload;
    private LocalDateTime timestamp;
}
