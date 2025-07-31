package com.yadong.yuchuang.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * 代码保存模板
 */
@Slf4j
public abstract class CodeFileSaverTemplate<T> {

    /**
     * 文件的前缀目录，当前项目下的 temp/code_output
     */
    private static final String FILE_SAVE_FIR = System.getProperty("user.dir") + "/temp/code_output/";


    public File saveCode(T codeContent, Long id) {
        // 1.校验数据
        validateCode(codeContent);
        // 2.构建唯一目录
        String basePath = buildUniqueDir(id);
        // 3.保存文件
        saveFiles(codeContent, basePath);
        // 4.返回文件保存的目录
        return new File(basePath);

    }

    /**
     * 将代码保存到文件中（子类实现）
     *
     * @param codeContent 代码内容
     */
    protected abstract void saveFiles(T codeContent, String uniqueDirPath);

    /**
     * 校验数据（子类可覆盖）
     *
     * @param codeContent 代码内容
     */
    protected void validateCode(T codeContent) {
        if (codeContent == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码内容不能为空");
        }
    }

    /**
     * 构建唯一目录
     *
     * @return 唯一目录
     */
    private String buildUniqueDir(Long id) {
        // 获取业务类型
        String bizType = getCodeType();
        // 目录名
        String uniqueDirName = StrFormatter.format("{}_{}", bizType, id);
        // 完整的保存目录
        String dirPath = FILE_SAVE_FIR + uniqueDirName;
        // 创建目录
        FileUtil.mkdir(dirPath);
        // 返回目录名
        return dirPath;
    }

    /**
     * 通用保存文件方法
     *
     * @param dirPath  文件保存目录
     * @param fileName 文件名
     * @param content  文件内容
     */
    protected void writeToFile(String dirPath, String fileName, String content) {
        if (StrUtil.isBlank(content)) {
            log.warn("文件内容为空，不保存");
            return;
        }
        // 文件路径
        String filePath = dirPath + File.separator + fileName;
        // 保存文件
        File file = FileUtil.writeString(content, filePath, CharsetUtil.CHARSET_UTF_8);
        // 打印日志
        log.info("保存文件成功：{}", file.getAbsolutePath());
    }


    /**
     * 获取代码类型（子类实现）
     *
     * @return bizType 业务类型
     */
    protected abstract String getCodeType();
}
