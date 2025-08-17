package com.yadong.yuchuang.ai;

import com.yadong.yuchuang.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建AiCodeGenTypeRoutingService
 */
@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建AiCodeGenTypeRoutingService接口，每次返回的 AiCodeGenTypeRoutingService实例都是新的
     *
     * @return AiCodeGenTypeRoutingService
     */
    public AiCodeGenTypeRoutingService getAiCodeGenTypeRoutingService() {
        ChatModel model = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(model)
                .build();
    }

    /**
     * 提供一个默认的bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return getAiCodeGenTypeRoutingService();
    }
}
