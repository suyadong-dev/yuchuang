package com.yadong.yuchuang.config;

import com.yadong.yuchuang.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;


/**
 * 通用流式模型配置，HTML、多文件模式使用
 */
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Configuration
@Data
public class StreamingChatModelModelConfig {

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    private String apiKey;

    private String baseUrl;

    private String modelName;

    private boolean logRequests;

    private boolean logResponses;

    @Bean
    @Scope("prototype")
    public StreamingChatModel streamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }

}
