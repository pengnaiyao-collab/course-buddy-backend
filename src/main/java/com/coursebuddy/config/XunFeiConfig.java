package com.coursebuddy.config;

import com.coursebuddy.client.XunFeiSparkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class XunFeiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "xunfei", name = "enabled", havingValue = "true", matchIfMissing = true)
    public XunFeiSparkClient xunFeiSparkClient(XunFeiProperties properties) {
        log.info("Initializing XunFei Spark client with endpoint: {}", properties.getEndpoint());
        return new XunFeiSparkClient(properties);
    }
}
