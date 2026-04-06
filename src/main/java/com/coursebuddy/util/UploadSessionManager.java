package com.coursebuddy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上传
 */
@Component
@Slf4j
public class UploadSessionManager {

    private final ConcurrentHashMap<String, UploadSession> sessions = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_KEY_PREFIX = "upload:session:";

    public String createSession(String fileName, long fileSize, String category,
                                int totalChunks, String objectName) {
        String sessionId = UUID.randomUUID().toString();
        UploadSession session = UploadSession.builder()
                .sessionId(sessionId)
                .fileName(fileName)
                .fileSize(fileSize)
                .category(category)
                .totalChunks(totalChunks)
                .objectName(objectName)
                .progress(0)
                .createdAt(LocalDateTime.now())
                .build();

        sessions.put(sessionId, session);

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, session,
                        Duration.ofHours(24));
            } catch (Exception e) {
                log.debug("Redis unavailable or error, falling back to local storage: {}", e.getMessage());
            }
        }

        log.info("Upload session created: {}", sessionId);
        return sessionId;
    }

    public UploadSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void updateChunkProgress(String sessionId, int chunkIndex) {
        UploadSession session = sessions.get(sessionId);
        if (session != null) {
            session.getUploadedChunks().put(chunkIndex, true);
            int progress = (int) ((session.getUploadedChunks().size() * 100.0)
                    / session.getTotalChunks());
            session.setProgress(progress);
        }
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(SESSION_KEY_PREFIX + sessionId);
            } catch (Exception e) {
                log.debug("Redis unavailable or error, local session already removed: {}", e.getMessage());
            }
        }
        log.info("Upload session removed: {}", sessionId);
    }

    public static String generateObjectName(String fileName) {
        // 清理文件名，移除路径分隔符和控制字符
        String safeName = fileName.replaceAll("[/\\\\\\u0000-\\u001F]", "_");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return "uploads/" + date + "/" + UUID.randomUUID() + "/" + safeName;
    }
}
