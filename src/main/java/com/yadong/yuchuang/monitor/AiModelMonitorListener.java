package com.yadong.yuchuang.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 模型监听器
 */
@Component
public class AiModelMonitorListener implements ChatModelListener {

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    public static final String REQUEST_START_TIME = "requestStartTime";

    /**
     * 请求开始时执行, 记录 AI 接口调用的次数
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // 从 ThreadLocal 中获取 appId、userId
        MonitorContext monitorContext = MonitorContextHolder.get();
        Long appId = monitorContext.getAppId();
        Long userId = monitorContext.getUserId();
        // 将 appId、userId 传递给 ChatModelResponseContext, 因为响应线程和请求线程不是一个线程，拿不到 monitorContext
        requestContext.attributes().put("appId", appId);
        requestContext.attributes().put("userId", userId);
        // 记录开始时间, 方便在响应结束时计算总体耗时
        requestContext.attributes().put(REQUEST_START_TIME, System.currentTimeMillis());
        // 记录请求次数
        String modelName = requestContext.chatRequest().modelName();
        aiModelMetricsCollector.recordRequest(appId, userId, modelName, "start");
    }

    /**
     * 响应结束时执行
     */
    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // 获取appId、userId
        Long appId = (Long) responseContext.attributes().get("appId");
        Long userId = (Long) responseContext.attributes().get("userId");
        // 获取开始时间
        long requestStartTime = (long) responseContext.attributes().get(REQUEST_START_TIME);
        // 响应时间
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - requestStartTime);
        // 记录响应时间
        String modelName = responseContext.chatResponse().modelName();
        aiModelMetricsCollector.recordResponseDuration(appId, userId, modelName, duration);
        // 记录 token 消耗数
        TokenUsage tokenUsage = responseContext.chatResponse().tokenUsage();
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(appId, userId, modelName, "input_token", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(appId, userId, modelName, "output_token", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(appId, userId, modelName, "total_token", tokenUsage.totalTokenCount());
        }
    }

    /**
     * 请求出现异常时执行
     */
    @Override
    public void onError(ChatModelErrorContext errorContext) {
        // 获取appId、userId
        Long appId = (Long) errorContext.attributes().get("appId");
        Long userId = (Long) errorContext.attributes().get("userId");
        String modelName = errorContext.chatRequest().modelName();
        // 记录失败请求
        aiModelMetricsCollector.recordRequest(appId, userId, modelName, "error");
        // 记录错误信息
        aiModelMetricsCollector.recordError(appId, userId, modelName, errorContext.error().getMessage());
        // 响应失败也记录响应时间
        long startTime = (long) errorContext.attributes().get(REQUEST_START_TIME);
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        aiModelMetricsCollector.recordResponseDuration(appId, userId, modelName, duration);
    }
}
