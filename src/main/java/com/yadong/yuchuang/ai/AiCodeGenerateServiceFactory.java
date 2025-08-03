package com.yadong.yuchuang.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yadong.yuchuang.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 创建AiCodeGenerateService
 */
@Configuration
@Slf4j
public class AiCodeGenerateServiceFactory {

    // 普通对象模型
    @Resource
    private ChatModel model;

    // 普通对象模型
    @Resource
    private StreamingChatModel streamingChatModel;

    // 缓存模型
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    // 聊天记录服务
    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<Long, AiCodeGenerateService> CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .removalListener((key, value, cause) -> {
                log.info("移除缓存，key：{}，value：{}，cause：{}", key, value, cause);
            })
            .build();

    /**
     * 获取app对应的AiCodeGenerateService
     *
     * @param appId appId
     * @return app对应的AiCodeGenerateService
     */
    public AiCodeGenerateService getAiCodeGenerateService(long appId) {
        return CACHE.get(appId, this::createAiCodeGenerateService);
    }

    /**
     * 将 AiCodeGenerateService 注入容器中
     *
     * @return AiCodeGenerateService
     */
    public AiCodeGenerateService createAiCodeGenerateService(long appId) {
        // 创建缓存模型
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .maxMessages(10)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
        // 加载历史对话到缓存中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGenerateService.class)
                .chatModel(model)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * 保留一个默认的，和以前的版本兼容
     */
    @Bean
    public AiCodeGenerateService createAiCodeGenerateService() {
        return getAiCodeGenerateService(0L);
    }
}
