package com.yadong.yuchuang.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 模型指标收集器
 */
@Component
public class AiModelMetricsCollector {

    /**
     * 指标注册中心
     */
    @Resource
    private MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, Counter> aiRequestCounter = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Timer> aiResponseTime = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Counter> aiErrorCounter = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Counter> aiTokensCounter = new ConcurrentHashMap<>();


    /**
     * 记录模型请求次数
     */
    public void recordRequest(long appId, long userId, String modelName, String status) {
        String key = String.format("%s_%s_%s_%s", appId, userId, modelName, status);
        Counter counter = aiRequestCounter.computeIfAbsent(key, k ->
                Counter.builder("ai_request_count")
                        .description("AI 调用请求次数")
                        .tag("app_id", String.valueOf(appId))
                        .tag("user_id", String.valueOf(userId))
                        .tag("model_name", modelName)
                        .tag("status", status)
                        .register(meterRegistry)
        );
        // 增加计数
        counter.increment();
    }


    /**
     * 记录模型响应时间
     */
    public void recordResponseDuration(long appId, long userId, String modelName, Duration duration) {
        String key = String.format("%s_%s_%s", appId, userId, modelName);
        Timer timer = aiResponseTime.computeIfAbsent(key, k ->
                Timer.builder("ai_response_duration")
                        .description("AI 请求响应时间")
                        .tag("app_id", String.valueOf(appId))
                        .tag("user_id", String.valueOf(userId))
                        .tag("model_name", modelName)
                        .register(meterRegistry)
        );
        // 增加计数
        timer.record(duration);
    }

    /**
     * 记录模型错误次数
     */
    public void recordError(long appId, long userId, String modelName, String error) {
        String key = String.format("%s_%s_%s_%s", appId, userId, modelName, error);
        Counter counter = aiErrorCounter.computeIfAbsent(key, k ->
                Counter.builder("ai_error_count")
                        .description("AI 请求错误次数")
                        .tag("app_id", String.valueOf(appId))
                        .tag("user_id", String.valueOf(userId))
                        .tag("model_name", modelName)
                        .tag("error", error)
                        .register(meterRegistry)
        );
        // 增加计数
        counter.increment();
    }

    /**
     * 记录模型消耗的token数
     */
    public void recordTokenUsage(long appId, long userId, String modelName, String tokenType, long tokens) {
        String key = String.format("%s_%s_%s", appId, userId, modelName);
        Counter counter = aiTokensCounter.computeIfAbsent(key, k ->
                Counter.builder("ai_tokens_count")
                        .description("AI 模型消耗的token数")
                        .tag("app_id", String.valueOf(appId))
                        .tag("user_id", String.valueOf(userId))
                        .tag("model_name", modelName)
                        .tag("token_type", tokenType)
                        .register(meterRegistry)
        );
        counter.increment(tokens);
    }
}