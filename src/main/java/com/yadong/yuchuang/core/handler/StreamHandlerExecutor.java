package com.yadong.yuchuang.core.handler;

import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import com.yadong.yuchuang.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class StreamHandlerExecutor {
    /**
     * VUE 项目流式处理器
     */
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * HTML、MULTI_FILE 流式处理器
     */
    private static final SimpleTextStreamHandler simpleTextStreamHandler = new SimpleTextStreamHandler();

    /**
     * 流处理执行器
     */
    public Flux<String> execute(Flux<String> codeStream, ChatHistoryService chatHistoryService,
                                User loginUser, long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case CodeGenTypeEnum.HTML, CodeGenTypeEnum.MULTI_FILE ->
                    simpleTextStreamHandler.handle(codeStream, chatHistoryService, loginUser, appId);
            case CodeGenTypeEnum.VUE_PROJECT ->
                    jsonMessageStreamHandler.handle(codeStream, chatHistoryService, loginUser, appId);
        };
    }
}
