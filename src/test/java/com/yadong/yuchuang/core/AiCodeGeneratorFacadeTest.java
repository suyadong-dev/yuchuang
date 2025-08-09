package com.yadong.yuchuang.core;

import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
@Slf4j
class AiCodeGeneratorFacadeTest {
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    public void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode(
                "生成用户“超人不会飞”的个人博客页面，不超过三十行”", 1L, CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    public void generateAndSaveCodeStream() {
        aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        "生成用户“超人不会飞”的个人博客页面，不超过二十行”", 2L, CodeGenTypeEnum.HTML)
                .subscribe(System.out::println);
    }

    @Test
    public void chatMemoryTest() {
        aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        "生成用户“超人不会飞”的个人博客页面，不超过二十行”", 2L, CodeGenTypeEnum.HTML)
                .subscribe(System.out::println);

        aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        "我刚刚让你做了什么", 2L, CodeGenTypeEnum.HTML)
                .subscribe(System.out::println);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "简单的停车场管理系统，总代码量不超过 200 行"
                , 3L, CodeGenTypeEnum.VUE_PROJECT);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

}