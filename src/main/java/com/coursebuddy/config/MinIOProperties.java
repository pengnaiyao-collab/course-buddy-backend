package com.coursebuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {
    private String endpoint = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucketName = "course-buddy";
    private long maxFileSize = 1073741824L; // 1 GB（字节）
    private int chunkSize = 5242880; // 5 MB（字节）
}
