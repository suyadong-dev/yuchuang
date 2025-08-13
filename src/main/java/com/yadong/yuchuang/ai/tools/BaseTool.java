package com.yadong.yuchuang.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * 工具基类
 */
public abstract class BaseTool {
    /**
     * 获取工具名称
     *
     * @return 工具名称
     */
    protected abstract String getToolName();

    /**
     * 获取工具显示名称（中文名称）
     *
     * @return 工具显示名称
     */
    protected abstract String getDisplayName();

    /**
     * 生成工具请求响应
     *
     * @return 工具请求响应
     */
    public String generateToolRequestsResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }


    /**
     * 生成工具执行结果
     */
    public abstract String generateToolExecutionResult(JSONObject arguments);
}
