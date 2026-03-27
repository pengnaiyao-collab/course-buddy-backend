package com.coursebuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {
    private String endpoint = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucketName = "course-buddy";
    private long maxFileSize = 1073741824L; // 1GB
    private int chunkSize = 5242880; // 5MB
}
