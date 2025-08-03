package com.yadong.yuchuang.core;

import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
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
}