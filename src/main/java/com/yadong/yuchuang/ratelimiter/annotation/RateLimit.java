package com.yadong.yuchuang.ratelimiter.annotation;

import com.yadong.yuchuang.ratelimiter.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 时间窗口（秒）
     */
    int rateInterval() default 1;

    /**
     * 每个时间窗口允许的请求数
     */
    int rate() default 10;

    /**
     * 限流类型
     */
    RateLimitType type() default RateLimitType.USER;

    /**
     * 限流描述
     */
    String description() default "AI 请求过于频繁，请稍后再试！";
}
