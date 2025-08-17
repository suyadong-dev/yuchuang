package com.yadong.yuchuang.core;

import cn.hutool.json.JSONUtil;
import com.yadong.yuchuang.ai.AiCodeGenerateService;
import com.yadong.yuchuang.ai.AiCodeGenerateServiceFactory;
import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import com.yadong.yuchuang.ai.model.message.AiResponseMessage;
import com.yadong.yuchuang.ai.model.message.ToolExecutedMessage;
import com.yadong.yuchuang.ai.model.message.ToolRequestMessage;
import com.yadong.yuchuang.core.builder.VueProjectBuilder;
import com.yadong.yuchuang.core.parser.CodeParserExecutor;
import com.yadong.yuchuang.core.saver.CodeFileSaverExecutor;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.exception.ThrowUtils;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

import static com.yadong.yuchuang.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;


/**
 * 代码生成门面类，一行代码生成并保存代码
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    @Lazy
    private AiCodeGenerateServiceFactory aiCodeGenerateServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;


    /**
     * 根据类型生成代码并保存（统一入口）
     */
    public File generateAndSaveCode(String userMessage, Long id, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "生成类型为空");
        // 每个app一个 服务，防止多个app之间互相影响，自动实现对话隔离
        AiCodeGenerateService aiCodeGenerateService = aiCodeGenerateServiceFactory.getAiCodeGenerateService(id);
        switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGenerateService.generateHtmlCode(userMessage);
                return CodeFileSaverExecutor.executeSave(htmlCodeResult, id, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGenerateService.generateMultiFileCode(userMessage);
                return CodeFileSaverExecutor.executeSave(multiFileCodeResult, id, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
    }

    /**
     * 生成代码并保存（流式统一入口）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "生成类型为空");
        // 获取当前app 的AiCodeGenerateService
        AiCodeGenerateService aiCodeGenerateService = aiCodeGenerateServiceFactory.getAiCodeGenerateService(appId, codeGenTypeEnum);
        switch (codeGenTypeEnum) {
            case HTML -> {
                // 生成代码
                Flux<String> codeStream = aiCodeGenerateService.generateHtmlCodeStream(userMessage);
                // 解析代码
                return processCodeStream(codeStream, appId, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                // 生成代码
                Flux<String> codeStream = aiCodeGenerateService.generateMultiFileCodeStream(userMessage);
                // 解析代码
                return processCodeStream(codeStream, appId, codeGenTypeEnum);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGenerateService.generateVueProjectCodeStream(appId, userMessage);
                return processTokenSteam(tokenStream, appId);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
    }

    /**
     * 将 tokenSteam流转为 Flux流
     *
     * @param tokenStream AI 生成的流
     * @return Flux流
     */
    private Flux<String> processTokenSteam(TokenStream tokenStream, long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse(s -> {
                        // AI 响应消息，如代码生成完毕
                        AiResponseMessage partialResponse = new AiResponseMessage(s);
                        sink.next(JSONUtil.toJsonStr(partialResponse));
                        log.info("AI 响应：{}", s);
                    })
                    // 工具执行请求
                    .onPartialToolExecutionRequest((idx, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                        log.info("工具执行请求：{}", toolExecutionRequest);
                    })
                    .onToolExecuted(toolExecution -> {
                        // 工具调用完毕消息
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                        log.info("工具调用完毕：{}", toolExecution);
                    })
                    .onCompleteResponse(chatResponse -> {
                        // 同步构建 vue 项目
                        String projectDir = String.format("%s/vue_project_%s", CODE_OUTPUT_ROOT_DIR, appId);
                        vueProjectBuilder.buildProject(new File(projectDir));
                        sink.complete();
                    })
                    .onError(throwable -> {
                        throwable.printStackTrace();
                        sink.error(throwable);
                    }).start();
        });

    }

    /**
     * 处理代码流
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 业务类型
     * @return 处理后的代码流
     */
    public Flux<String> processCodeStream(Flux<String> codeStream, Long id, CodeGenTypeEnum codeGenTypeEnum) {
        StringBuilder code = new StringBuilder();
        return codeStream.doOnNext(code::append)
                // 处理完成后保存
                .doOnComplete(() -> {
                    try {
                        // 解析代码
                        Object parseResult = CodeParserExecutor.executeParse(code.toString(), codeGenTypeEnum);
                        // 保存代码
                        File file = CodeFileSaverExecutor.executeSave(parseResult, id, codeGenTypeEnum);
                        // 记录日志
                        log.info("代码生成完毕，保存文件成功：{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.info("解析代码失败：{}", e.getMessage());
                    }
                });
    }
}
