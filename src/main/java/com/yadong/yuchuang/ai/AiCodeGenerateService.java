package com.yadong.yuchuang.ai;

import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import com.yadong.yuchuang.annonation.RecordTokenUsage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGenerateService {

    /**
     * 生成单页面 HTML 代码
     *
     * @param userMessage 用户消息
     * @return 响应结果
     */
    // @SystemMessage 设置系统消息
    @SystemMessage(fromResource = "prompt/html-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_HTML")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户消息
     * @return 响应结果
     */
    @SystemMessage(fromResource = "prompt/multi-file-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_MULTI_FILE")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 生成单页面 HTML 代码（流式）
     *
     * @param userMessage 用户消息
     * @return 响应结果
     */
    // @SystemMessage 设置系统消息
    @SystemMessage(fromResource = "prompt/html-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_HTML_STREAM")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 响应结果
     */
    @SystemMessage(fromResource = "prompt/multi-file-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_MULTI_FILE_STREAM")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成Vue工程项目（流式）
     *
     * @param appId       应用id 主要是为了AI工具调用时使用
     * @param userMessage 用户消息
     * @return 响应结果
     */
    @SystemMessage(fromResource = "prompt/vue-project-system-message.txt")
    @RecordTokenUsage(type = "GENERATE_VUE_PROJECT_STREAM")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);
}