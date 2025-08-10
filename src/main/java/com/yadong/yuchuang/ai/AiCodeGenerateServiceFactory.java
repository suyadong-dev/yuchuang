package com.yadong.yuchuang.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yadong.yuchuang.ai.tools.FileWriteTool;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import com.yadong.yuchuang.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private StreamingChatModel openAiStreamingChatModel;

    // 推理模型
    @Resource
    @Qualifier("reasoningStreamingChatModel")
    private StreamingChatModel reasoningStreamingChatModel;

    // 缓存模型
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    // 聊天记录服务
    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<String, AiCodeGenerateService> CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .removalListener((key, value, cause) -> {
                log.info("移除缓存，key：{}，value：{}，cause：{}", key, value, cause);
            })
            .build();

    /**
     * 获取app对应的AiCodeGenerateService（为了兼容老逻辑）
     *
     * @param appId appId
     * @return app对应的AiCodeGenerateService
     */
    public AiCodeGenerateService getAiCodeGenerateService(long appId) {
        return getAiCodeGenerateService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * @param appId           appId
     * @param codeGenTypeEnum app对应的AiCodeGenerateService
     */
    public AiCodeGenerateService getAiCodeGenerateService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        String cacheKey = buildCacheKey(appId, codeGenTypeEnum);
        return CACHE.get(cacheKey, key -> createAiCodeGenerateService(appId, codeGenTypeEnum));
    }

    /**
     * 将 AiCodeGenerateService 注入容器中
     *
     * @return AiCodeGenerateService
     */
    public AiCodeGenerateService createAiCodeGenerateService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        // 创建缓存模型
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .maxMessages(10)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
        // 加载历史对话到缓存中
        int count = chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        log.info("加载历史消息成功，共加载{}条", count);
        return switch (codeGenTypeEnum) {
            // Vue项目生成使用推理模型
            case VUE_PROJECT -> AiServices.builder(AiCodeGenerateService.class)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(new FileWriteTool())
                    // 幻觉工具名称策略
                    .hallucinatedToolNameStrategy(toolExecutionRequest ->
                            ToolExecutionResultMessage.from(toolExecutionRequest,
                                    "Error: there is no tool called" + toolExecutionRequest.name())
                    )
                    .build();
            // 原生html和前端三件套使用普通模型
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGenerateService.class)
                    .chatModel(model)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
        };
    }

    /**
     * 保留一个默认的，和以前的版本兼容
     */
    @Bean
    public AiCodeGenerateService createAiCodeGenerateService() {
        return getAiCodeGenerateService(0L);
    }

    /**
     * 根据生成的应用类型构建缓存key
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return codeGenTypeEnum.getValue() + "_" + appId;
    }
}
