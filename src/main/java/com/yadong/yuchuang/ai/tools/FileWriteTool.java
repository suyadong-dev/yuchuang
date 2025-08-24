package com.yadong.yuchuang.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.yadong.yuchuang.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

@Slf4j
@Component
public class FileWriteTool extends BaseTool {
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
            File file = new File(filePath);
            
            // 2.检查文件是否已存在
            if (file.exists()) {
                log.info("文件已存在，跳过写入:{}", filePath);
                // 3.统计已经生成的文件目录，并拼接给 AI，防止其重复生成文件
                List<String> writtenFileList = getWrittenFileList(projectDirName);
                String writtenFileListStr = String.format("\n目前一共生成的文件如下：%s\n注意：不要重复生成文件", writtenFileList.toString());
                return "文件已存在，跳过写入" + relativeFilePath + writtenFileListStr;
            }
            
            // 3.确保父目录存在
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            
            // 4.写入文件
            FileUtil.writeString(content, filePath, CharsetUtil.UTF_8);
            log.info("文件写入成功:{}", filePath);
            
            // 5.统计已经生成的文件目录，并拼接给 AI，防止其重复生成文件
            List<String> writtenFileList = getWrittenFileList(projectDirName);
            String writtenFileListStr = String.format("\n目前一共生成的文件如下：%s\n注意：不要重复生成文件", writtenFileList.toString());
            return "文件写入成功" + relativeFilePath + writtenFileListStr; // 字符串类型会直接返回给 AI
        } catch (IORuntimeException e) {
            String errorMessage = "文件写入失败：" + e.getMessage() + "文件路径：" + relativeFilePath;
            log.error(e.getMessage(), errorMessage);
            // 返回异常信息给 AI 会纠正其错误并在认为必要时重试
            return errorMessage;
        }
    }

    @Override
    protected String getToolName() {
        return "writeFile";
    }

    @Override
    protected String getDisplayName() {
        return "写入文件";
    }

    @Override
    public String generateToolExecutionResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String content = arguments.getStr("content");
        return String.format("""
                [工具调用] %s %s
                ```%s
                %s
                ```
                """, getDisplayName(), relativeFilePath, suffix, content);
    }

    /**
     * 获取已经写入的文件列表
     *
     * @param projectDirName 项目路径
     */
    private static List<String> getWrittenFileList(String projectDirName) {
        File directory = new File(projectDirName);

        // 1.获取目录下所有文件（递归，包含子目录中的文件）
        List<File> fileList = FileUtil.loopFiles(directory);

        // 2. 创建一个 Path 对象，表示基准目录
        Path basePath = directory.toPath();
        // 3. 遍历每个文件，计算其相对于基准目录的路径
        // 统一使用正斜杠作为路径分隔符，确保跨平台一致性
        return fileList.stream()
                .map(file -> basePath.relativize(file.toPath()).toString().replace('\\', '/'))
                .toList();
    }
}
