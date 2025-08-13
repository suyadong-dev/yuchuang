package com.yadong.yuchuang.ai.model;

import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成应用属性")
@Data
public class AppProperty {

    @Description("应用名称")
    private String appName;

    @Description("代码生成类型")
    private CodeGenTypeEnum codeGenType;
}
