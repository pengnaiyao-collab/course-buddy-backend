package com.coursebuddy.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置
 */
@Slf4j
@Configuration
public class MinIOConfig {

    @Bean
    public MinioClient minioClient(MinIOProperties properties) {
        log.info("Initializing MinIO client with endpoint: {}", properties.getEndpoint());
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
