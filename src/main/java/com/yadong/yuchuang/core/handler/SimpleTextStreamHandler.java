package com.yadong.yuchuang.core.handler;

import com.yadong.yuchuang.model.entity.ChatHistory;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.enums.ChatMessageTypeEnum;
import com.yadong.yuchuang.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流式处理器
 * 处理 HTML、MULTI_FILE 模式生成的流式数据
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流，HTML， MULTI_FILE
     *
     * @param codeStream         AI 生成的流
     * @param chatHistoryService 对话历史服务
     * @param loginUser          登录用户
     * @param appId              应用id
     */
    public Flux<String> handle(Flux<String> codeStream, ChatHistoryService chatHistoryService,
                               User loginUser, long appId) {
        StringBuilder aiResponse = new StringBuilder();
        return codeStream
                .doOnNext(aiResponse::append)
                .doOnComplete(() -> {
                    // 6.将AI的回答保存到数据库
                    chatHistoryService.addChatMessage(ChatHistory.builder()
                            .message(aiResponse.toString())
                            .messageType(ChatMessageTypeEnum.AI.getValue())
                            .userId(loginUser.getId())
                            .appId(appId)
                            .build());
                })
                .doOnError(e -> {
                    log.info("AI 生成代码失败：{}", e.getMessage());
                    // 生成失败也保存
                    chatHistoryService.addChatMessage(ChatHistory.builder()
                            .message(aiResponse.toString())
                            .messageType(ChatMessageTypeEnum.AI.getValue())
                            .userId(loginUser.getId())
                            .appId(appId)
                            .build());
                });
    }
}
