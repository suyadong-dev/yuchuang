package com.yadong.yuchuang.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * 代码保存器
 */
@Slf4j
public class CodeFileSaver {

    /**
     * 文件的前缀目录，当前项目下的 temp/code_output
     */
    private static final String FILE_SAVE_FIR = System.getProperty("user.dir") + "/temp/code_output/";

    /**
     * 保存单文件html代码
     */
    public static void saveHtmlCode(HtmlCodeResult htmlCodeResult) {
        if (htmlCodeResult == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String filePath = buildUniqueDir("html") + "/";
        writeToFile(filePath, "index.html", htmlCodeResult.getHtmlCode());
    }

    /**
     * 保存多文件代码
     */
    public static void saveMultiFileCode(MultiFileCodeResult multiFileCodeResult) {
        if (multiFileCodeResult == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        // 构建文件保存目录
        String filePath = buildUniqueDir("multi_file") + "/";
        // 分别保存文件
        writeToFile(filePath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(filePath, "index.css", multiFileCodeResult.getCssCode());
        writeToFile(filePath, "index.js", multiFileCodeResult.getJsCode());
    }


    /**
     * 构建唯一目录
     *
     * @param bizType 业务类型
     * @return 唯一目录
     */
    private static String buildUniqueDir(String bizType) {
        return FILE_SAVE_FIR + bizType + IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 通用保存文件方法
     *
     * @param dirPath  文件保存目录
     * @param fileName 文件名
     * @param content  文件内容
     */
    private static void writeToFile(String dirPath, String fileName, String content) {
        // 文件路径
        String filePath = dirPath + File.separator + fileName;
        // 保存文件
        File file = FileUtil.writeString(content, filePath, CharsetUtil.CHARSET_UTF_8);
        // 打印日志
        log.info("保存文件成功：{}", file.getAbsolutePath());
    }
}

