package com.yadong.yuchuang.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * html代码生成结果
 * 结构化输出结果
 */
@Data
public class HtmlCodeResult {

    @Description("html代码")
    private String htmlCode;

    @Description("html代码描述")
    private String description;
}
