package com.yadong.yuchuang.core.parser;

import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 * 根据代码生成类型执行对应的代码解析器
 *
 * @author 超人不会飞
 */
public class CodeParserExecutor {
    /**
     * HTML代码解析器
     */
    private static final CodeParser<HtmlCodeResult> HTML_CODE_PARSER = new HtmlCodeParser();

    /**
     * 多文件代码解析器
     */
    private static final CodeParser<MultiFileCodeResult> MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 执行代码解析
     *
     * @param content         代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果
     */
    public static Object executeParse(String content, CodeGenTypeEnum codeGenTypeEnum) {
        switch (codeGenTypeEnum) {
            case CodeGenTypeEnum.HTML -> {
                return HTML_CODE_PARSER.parseCode(content);
            }
            case CodeGenTypeEnum.MULTI_FILE -> {
                return MULTI_FILE_CODE_PARSER.parseCode(content);
            }
            default -> {
                throw new IllegalArgumentException("不支持的代码生成类型：" + codeGenTypeEnum);
            }
        }
    }
}
