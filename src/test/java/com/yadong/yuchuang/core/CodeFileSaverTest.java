package com.yadong.yuchuang.core;

import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodeFileSaverTest {
    @Test
    void testSaveHtmlCode() {
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        htmlCodeResult.setHtmlCode("<html><h1>Hello World!</h1></html>");
        htmlCodeResult.setDescription("这是测试的html代码");
        CodeFileSaver.saveHtmlCode(htmlCodeResult);
    }

    @Test
    void saveMultiFileCode() {
        MultiFileCodeResult multiFileCodeResult = new MultiFileCodeResult();
        multiFileCodeResult.setHtmlCode("<html><h1>Hello World!</h1></html>");
        multiFileCodeResult.setCssCode("h1 {color: red;}");
        multiFileCodeResult.setJsCode("alert('Hello World!');");
        multiFileCodeResult.setDescription("这是测试的多个代码");
        CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
    }
}