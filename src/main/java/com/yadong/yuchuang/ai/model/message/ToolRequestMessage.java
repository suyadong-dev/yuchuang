package com.yadong.yuchuang.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolRequestMessage extends StreamMessage {
    /**
     * 工具id
     */
    private String id;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具调用参数
     */
    private String arguments;

    public ToolRequestMessage(ToolExecutionRequest toolExecutionRequest) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = toolExecutionRequest.id();
        this.name = toolExecutionRequest.name();
        this.arguments = toolExecutionRequest.arguments();
    }
}
