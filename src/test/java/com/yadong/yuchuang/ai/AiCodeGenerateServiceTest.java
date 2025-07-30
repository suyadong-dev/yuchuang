package com.yadong.yuchuang.ai;

import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGenerateServiceTest {
    @Resource
    private AiCodeGenerateService aiCodeGenerateService;

    @Test
    void testChat() {
        String result = aiCodeGenerateService.chat("写一个java程序，打印一个hello world");
        System.out.println(result);
    }

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCodeResult = aiCodeGenerateService.generateHtmlCode("写一个登录页面，代码不超过二十行");
        Assertions.assertNotNull(htmlCodeResult);
    }

    @Test
    void generateMultiFileCode() {
    }
}