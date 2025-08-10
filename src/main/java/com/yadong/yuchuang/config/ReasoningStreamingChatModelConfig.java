package com.yadong.yuchuang.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Configuration
@Data
public class ReasoningStreamingChatModelConfig {
    private String apiKey;
    private String baseUrl;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 生成环境使用
        final String modelName = "deepseek-reasoner";
        final int maxTokens = 32768;
//        final String modelName = "deepseek-chat";
//        final int maxTokens = 4096;
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

}
