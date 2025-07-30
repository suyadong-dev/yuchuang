package com.yadong.yuchuang.core;

import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeParserTest {

    @Test
    void parseHtmlCode() {
        String codeContent = """
                您好，按照您的描述生成的HTML代码如下
                ```html
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>超人不会飞 - 个人博客</title>
                </head>
                <body>
                    <header>
                        <h1>超人不会飞</h1>
                        <p>记录生活与技术的点滴</p>
                    </header>
                </body>
                </html>
                ```
                模拟一段描述
                """;
        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(codeContent);
        System.out.println(htmlCodeResult.getHtmlCode());
        assertNotNull(htmlCodeResult);
    }

    @Test
    void parseMultiFileCode() {
    }
}