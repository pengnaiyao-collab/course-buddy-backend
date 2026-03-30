package com.coursebuddy.client;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;

import java.util.List;

public class AIChatClient {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public AIChatClient(ChatModel chatModel, StreamingChatModel streamingChatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    public AIChatResult chat(List<ChatMessage> messages) {
        ChatResponse response = chatModel.chat(messages);
        return new AIChatResult(
                safeText(response),
                inputTokens(response),
                outputTokens(response)
        );
    }

    public void chatStream(List<ChatMessage> messages, AIStreamListener listener) {
        streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                listener.onToken(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                listener.onComplete(inputTokens(completeResponse), outputTokens(completeResponse));
            }

            @Override
            public void onError(Throwable error) {
                listener.onError(error);
            }
        });
    }

    private String safeText(ChatResponse response) {
        return response == null || response.aiMessage() == null || response.aiMessage().text() == null
                ? ""
                : response.aiMessage().text();
    }

    private int inputTokens(ChatResponse response) {
        return getTokenUsageValue(response, true);
    }

    private int outputTokens(ChatResponse response) {
        return getTokenUsageValue(response, false);
    }

    private int getTokenUsageValue(ChatResponse response, boolean input) {
        if (response == null || response.metadata() == null) {
            return 0;
        }
        TokenUsage tokenUsage = response.metadata().tokenUsage();
        if (tokenUsage == null) {
            return 0;
        }
        Integer value = input ? tokenUsage.inputTokenCount() : tokenUsage.outputTokenCount();
        return value == null ? 0 : value;
    }

    public record AIChatResult(String content, int promptTokens, int completionTokens) {
        public int totalTokens() {
            return promptTokens + completionTokens;
        }
    }

    public interface AIStreamListener {
        void onToken(String token);

        void onComplete(int promptTokens, int completionTokens);

        void onError(Throwable throwable);
    }
}
