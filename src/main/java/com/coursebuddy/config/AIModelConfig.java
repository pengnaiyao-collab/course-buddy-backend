package com.coursebuddy.config;

import com.coursebuddy.client.AIChatClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI配置
 */
@Slf4j
@Configuration
public class AIModelConfig {

    @Bean
    @ConditionalOnProperty(prefix = "ai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AIChatClient aiChatClient(AIProperties properties) {
        Duration timeout = Duration.ofMillis(properties.getTimeout());

        ChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .temperature(properties.getTemperature())
                .topP(properties.getTopP())
                .maxTokens(properties.getMaxTokens())
                .timeout(timeout)
                .logRequests(properties.isLogRequests())
                .logResponses(properties.isLogResponses())
                .build();

        StreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .temperature(properties.getTemperature())
                .topP(properties.getTopP())
                .maxTokens(properties.getMaxTokens())
                .timeout(timeout)
                .logRequests(properties.isLogRequests())
                .logResponses(properties.isLogResponses())
                .build();

        log.info("Initializing AI model client with provider={}, baseUrl={}, model={}",
                properties.getProvider(), properties.getBaseUrl(), properties.getModel());

        return new AIChatClient(chatModel, streamingChatModel);
    }
}
