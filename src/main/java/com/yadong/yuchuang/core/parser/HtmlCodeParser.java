package com.yadong.yuchuang.core.parser;

import com.yadong.yuchuang.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;

/**
 * HTML代码解析器
 *
 * @author 超人不会飞
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    /**
     * 解析 HTML 单文件代码
     */
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 提取HTML代码内容
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private static String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
