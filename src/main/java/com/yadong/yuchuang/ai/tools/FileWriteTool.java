package com.yadong.yuchuang.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.CharsetUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static com.yadong.yuchuang.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

@Slf4j
public class FileWriteTool {
    /**
     * 文件写入工具
     * 支持 AI 将生成的代码写入到文件中
     *
     * @param relativeFilePath 文件的相对路径
     * @param content          文件内容
     * @param appId            应用的id
     * @return 写入的结果
     */
    @Tool("写入文件到指定路径")
    public static String writeFile(@P("文件的相对路径") String relativeFilePath,
                                   @P("要写入文件的代码") String content,
                                   @ToolMemoryId long appId) {
        try {
            // 1.拼接完整目录
            String projectDirName = CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
            String filePath = projectDirName + File.separator + relativeFilePath;
            // 2.写入文件
            FileUtil.writeString(content, filePath, CharsetUtil.UTF_8);
            log.info("文件写入成功:{}", filePath);
            // 3.返回路径
            return "文件写入成功" + relativeFilePath; // 字符串类型会直接返回给 AI
        } catch (IORuntimeException e) {
            String errorMessage = "文件写入失败：" + e.getMessage() + "文件路径：" + relativeFilePath;
            log.error(e.getMessage(), errorMessage);
            // 返回异常信息给 AI 会纠正其错误并在认为必要时重试
            return errorMessage;
        }
    }

//    public static void main(String[] args) {
//        writeFile("index.vue", "This is a test2", 1);
//    }
}
