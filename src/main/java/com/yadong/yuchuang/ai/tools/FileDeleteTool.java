package com.yadong.yuchuang.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.yadong.yuchuang.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * 文件删除工具
 * 支持 AI 通过工具调用的方式删除文件
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {
    /**
     * 文件删除工具
     * 支持 AI 将指定位置的代码删除
     *
     * @param relativeFilePath 文件的相对路径
     * @param appId            应用的id
     * @return 写入的结果
     */
    @Tool("删除指定路径的文件")
    public String deleteFile(@P("文件的相对路径") String relativeFilePath,
                             @ToolMemoryId long appId) {
        // 1.拼接完整目录
        String projectDirName = String.format("%s/vue_project_%s", CODE_OUTPUT_ROOT_DIR, appId);
        String fullFilePath = Paths.get(projectDirName, relativeFilePath).toString();

        // 2.校验文件是否存在
        if (!FileUtil.exist(fullFilePath)) {
            log.info("文件{}不存在，无需删除", fullFilePath);
            return "警告：文件不存在，无需删除";
        }
        // 3.重要的文件不能删除
        if (isImportantFile(fullFilePath)) {
            log.info("文件{}是重要的文件，不允许删除", fullFilePath);
            return "错误：不允许删除删除重要的文件" + relativeFilePath;
        }
        // 4.删除文件
        try {
            Files.delete(Paths.get(fullFilePath));
            log.info("删除文件成功：{}", fullFilePath);
            return "删除文件成功：" + relativeFilePath;
        } catch (IOException e) {
            log.error("删除文件失败：{}, 错误信息：{}", fullFilePath, e.getMessage());
            return "删除文件失败：" + relativeFilePath;
        }
    }

    /**
     * 判断是否是重要文件，不允许删除
     */
    private boolean isImportantFile(String fileName) {
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "vite.config.js", "vite.config.ts", "vue.config.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };
        for (String important : importantFiles) {
            if (important.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getToolName() {
        return "deleteFile";
    }

    @Override
    protected String getDisplayName() {
        return "删除文件66666666666";
    }

    @Override
    public String generateToolExecutionResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format(" [工具调用666666666666] %s %s", getDisplayName(), relativeFilePath);
    }
}
