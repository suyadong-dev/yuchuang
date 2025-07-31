package com.yadong.yuchuang.core.saver;

import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 文件保存执行器
 */
public class CodeFileSaverExecutor {
    /**
     * HTML 文件保存执行器
     */
    public static HtmlCodeSaverTemplate htmlCodeSaver = new HtmlCodeSaverTemplate();

    /**
     * 多文件保存执行器
     */
    public static MultiFileCodeSaverTemplate multiFileCodeSaver = new MultiFileCodeSaverTemplate();

    /**
     * 保存代码
     *
     * @param content         代码内容
     * @param codeGenTypeEnum 业务类型
     * @return 保存的文件
     */
    public static File executeSave(Object content, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML:
                yield htmlCodeSaver.saveCode((HtmlCodeResult) content);
            case MULTI_FILE:
                yield multiFileCodeSaver.saveCode((MultiFileCodeResult) content);
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
