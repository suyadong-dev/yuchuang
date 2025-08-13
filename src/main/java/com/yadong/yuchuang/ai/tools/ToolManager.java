package com.yadong.yuchuang.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ToolManager {
    /**
     * 工具名称到工具的映射
     */
    private Map<String, BaseTool> toolMap = new HashMap<>();

    /**
     * 所有工具
     */
    @Resource
    BaseTool[] tools;

    @PostConstruct
    public void initTool() {
        for (BaseTool tool : tools) {
            log.info("初始化工具：{}", tool.getToolName());
            toolMap.put(tool.getToolName(), tool);
        }
        log.info("工具初始化完成，共{}个工具", toolMap.size());
    }

    /**
     * 通过工具名获取工具
     *
     * @param toolName 工具名
     * @return 工具
     */
    public BaseTool getToolByName(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取所有的工具
     *
     * @return 工具列表
     */
    public BaseTool[] getAllTools() {
        return tools;
    }
}
