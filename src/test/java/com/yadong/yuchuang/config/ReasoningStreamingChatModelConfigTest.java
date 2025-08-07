package com.yadong.yuchuang.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class ReasoningStreamingChatModelConfigTest {

    @Resource
    @Qualifier("reasoningStreamingChatModel")
    private StreamingChatModel reasoningSteamingChatModel;

    @Test
    void reasoningStreamingChatModel() {
        reasoningSteamingChatModel.chat("给我将一个笑话", new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.println("onPartialResponse: " + partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println("onCompleteResponse: " + completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });
    }
}