package com.yadong.yuchuang.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 创建AiCodeGenTypeRoutingService
 */
@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 普通对象模型
     */
    @Resource
    private ChatModel model;

    public AiCodeGenTypeRoutingService getAiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(model)
                .build();
    }
}
