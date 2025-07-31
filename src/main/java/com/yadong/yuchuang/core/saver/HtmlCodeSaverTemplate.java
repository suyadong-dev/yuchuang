package com.yadong.yuchuang.core.saver;

import cn.hutool.core.util.StrUtil;
import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;

/**
 * HTML 模式代码保存器
 */
public class HtmlCodeSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected void saveFiles(HtmlCodeResult codeContent, String uniqueDirPath) {
        writeToFile(uniqueDirPath, "index.html", codeContent.getHtmlCode());
    }

    @Override
    protected String getCodeType() {
        return CodeGenTypeEnum.HTML.getValue();
    }

    @Override
    protected void validateCode(HtmlCodeResult codeContent) {
        super.validateCode(codeContent);
        if (StrUtil.isBlank(codeContent.getHtmlCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML代码不能为空");
        }
    }
}
