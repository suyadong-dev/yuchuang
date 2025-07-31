package com.yadong.yuchuang.core.parser;

import java.util.regex.Pattern;

/**
 * 代码解析器
 * 提供静态方法解析不同类型的代码内容
 *
 * @author 超人不会飞
 */
public interface CodeParser<T> {
    /**
     * HTML代码正则
     */
    Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * CSS代码正则
     */
    Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * JS代码正则
     */
    Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析代码
     *
     * @param codeContent 代码内容
     * @return 解析结果
     */
    T parseCode(String codeContent);
}
