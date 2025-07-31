package com.yadong.yuchuang.core.saver;

import cn.hutool.core.util.StrUtil;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;

/**
 * 多文件模式代码保存模板
 */
public class MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected void saveFiles(MultiFileCodeResult multiFileCodeResult, String uniqueDirPath) {
        // 分别保存文件
        writeToFile(uniqueDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(uniqueDirPath, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(uniqueDirPath, "script.js", multiFileCodeResult.getJsCode());
    }

    @Override
    protected String getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE.getValue();
    }

    @Override
    protected void validateCode(MultiFileCodeResult multiFileCodeResult) {
        // 至少要有HTML代码
        if (StrUtil.isBlank(multiFileCodeResult.getHtmlCode())) {
            throw new RuntimeException("HTML代码不能为空");
        }
    }
}
