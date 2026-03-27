package com.coursebuddy.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@Slf4j
public class CollaborationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public CollaborationWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle collaboration events broadcast to all session participants.
     * Client sends to: /app/collaboration/{sessionId}
     * Clients subscribe to: /topic/collaboration/{sessionId}
     */
    @MessageMapping("/collaboration/{sessionId}")
    @SendTo("/topic/collaboration/{sessionId}")
    public CollaborationMessage handleCollaborationEvent(
            @DestinationVariable String sessionId,
            CollaborationMessage message) {
        message.setSessionId(sessionId);
        message.setTimestamp(LocalDateTime.now());
        log.debug("Collaboration event in session {}: type={}", sessionId, message.getType());
        return message;
    }

    /**
     * Handle private messages to a specific user.
     * Client sends to: /app/collaboration/private/{userId}
     */
    @MessageMapping("/collaboration/private/{userId}")
    public void handlePrivateMessage(
            @DestinationVariable String userId,
            CollaborationMessage message) {
        message.setTimestamp(LocalDateTime.now());
        log.debug("Private collaboration message to user {}", userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/collaboration", message);
    }
}
