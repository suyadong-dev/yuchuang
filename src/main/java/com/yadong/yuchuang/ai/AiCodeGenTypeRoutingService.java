package com.yadong.yuchuang.ai;

import com.yadong.yuchuang.annonation.RecordTokenUsage;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI 路由代码生成类型服务
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * AI 选择代码生成类型
     *
     * @param prompt 用户代码生成提示词
     * @return 代码生成类型枚举
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_CODEGEN_ROUTING")
    CodeGenTypeEnum routeCodeGenType(String prompt);

    /**
     * AI 根据提示词生成应用名称
     */
    @SystemMessage(fromResource = "prompt/app-name-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_APP_NAME")
    String generateAppName(String prompt);

    /**
     * AI 根据提示词生成应用属性，包括应用名称和生成类型
     */
    @SystemMessage(fromResource = "prompt/app-property-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_APP_PROPERTY")
    String generateAppProperty(String prompt);
}
