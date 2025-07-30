package com.yadong.yuchuang.core;

import com.yadong.yuchuang.ai.AiCodeGenerateService;
import com.yadong.yuchuang.ai.model.HtmlCodeResult;
import com.yadong.yuchuang.ai.model.MultiFileCodeResult;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


/**
 * 代码生成门面类，一行代码生成并保存代码
 */
@Service
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGenerateService aiCodeGenerateService;

    /**
     * 生成代码并保存
     */
    public void generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
    }

    /**
     * 生成代码并保存（流式）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        switch (codeGenTypeEnum) {
            case HTML -> {
                return generateAndSaveHtmlCodeStream(userMessage);
            }
            case MULTI_FILE -> {
                return generateAndSaveMultiFileCodeStream(userMessage);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
    }

    /**
     * 生成并保存HTML代码
     *
     * @param userMessage 用户消息
     */
    private void generateAndSaveMultiFileCode(String userMessage) {
        // 生成代码
        MultiFileCodeResult multiFileCodeResult = aiCodeGenerateService.generateMultiFileCode(userMessage);
        // 保存文件
        CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
    }

    /**
     * 生成多个文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 流式响应
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        // 创建一个StringBuilder对象，用于存储HTML代码
        StringBuilder htmlCode = new StringBuilder();
        Flux<String> htmlCodeStream = aiCodeGenerateService.generateHtmlCodeStream(userMessage);
        return htmlCodeStream.doOnNext(htmlCode::append)
                // 当生成完毕时保存HTML代码
                .doOnComplete(() -> {
                    // 解析HTML代码
                    HtmlCodeResult code = CodeParser.parseHtmlCode(htmlCode.toString());
                    // 将 HTML代码保存到文件
                    CodeFileSaver.saveHtmlCode(code);
                });
    }

    /*
     * 生成并保存HTML、CSS、JS代码
     * @param userMessage 用户消息
     */
    private void generateAndSaveHtmlCode(String userMessage) {
        // 生成代码
        HtmlCodeResult htmlCodeResult = aiCodeGenerateService.generateHtmlCode(userMessage);
        // 保存代码
        CodeFileSaver.saveHtmlCode(htmlCodeResult);
    }

    /**
     * 生成并保存 HTML 代码（流式）
     *
     * @param userMessage 用户输入
     * @return 流式数据
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        StringBuilder multiFileCode = new StringBuilder();
        Flux<String> multiFileCodeStream = aiCodeGenerateService.generateMultiFileCodeStream(userMessage);
        return multiFileCodeStream.doOnNext(multiFileCode::append)
                .doOnComplete(() -> {
                    // 解析多文件代码
                    MultiFileCodeResult code = CodeParser.parseMultiFileCode(multiFileCode.toString());
                    // 保存多文件代码
                    CodeFileSaver.saveMultiFileCode(code);
                });
    }


}
