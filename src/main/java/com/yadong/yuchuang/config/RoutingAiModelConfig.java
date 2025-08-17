package com.yadong.yuchuang.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 路由专用模型配置
 */
@ConfigurationProperties(prefix = "langchain4j.open-ai.routing-chat-model")
@Configuration
@Data
public class RoutingAiModelConfig {
    private String apiKey;

    private String baseUrl;

    private String modelName;

    private boolean logRequests;

    private boolean logResponses;

    @Bean
    @Scope("prototype")
    public ChatModel routingChatModelPrototype() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

}
