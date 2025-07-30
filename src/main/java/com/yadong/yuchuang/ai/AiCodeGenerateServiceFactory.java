package com.yadong.yuchuang.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建AiCodeGenerateService
 */
@Configuration
public class AiCodeGenerateServiceFactory {

    /**
     * 普通对象模型
     */
    @Resource
    private ChatModel model;

    /**
     * 流式模型
     */
    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 将 AiCodeGenerateService 注入容器中
     *
     * @return AiCodeGenerateService
     */
    @Bean
    public AiCodeGenerateService createAiCodeGenerateService() {
        return AiServices.builder(AiCodeGenerateService.class)
                .chatModel(model)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
