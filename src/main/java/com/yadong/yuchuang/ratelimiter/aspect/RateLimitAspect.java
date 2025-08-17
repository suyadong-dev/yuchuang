package com.yadong.yuchuang.ratelimiter.aspect;

import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.ratelimiter.annotation.RateLimit;
import com.yadong.yuchuang.ratelimiter.enums.RateLimitType;
import com.yadong.yuchuang.service.UserService;
import com.yadong.yuchuang.utils.IpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 限流切面类
 */
@Aspect
@Component
public class RateLimitAspect {
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;

    /**
     * 在方法执行前拦截
     */
    @Before("@annotation(rateLimit)")
    public void doRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        String key = generateLimitKey(joinPoint, rateLimit);
        // 使用 Redisson 的分布式限流器
        RRateLimiter limiter = redissonClient.getRateLimiter(key);
        // 指定过期时间
        limiter.expire(Duration.ofHours(1L));
        // 指定限流策略
        limiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.rateInterval(), RateIntervalUnit.SECONDS);
        // 尝试获取令牌，获取失败则抛出异常
        if (!limiter.tryAcquire(1)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.description());
        }
    }

    /**
     * 生成限流key
     *
     * @param joinPoint 切点
     * @return 限流key
     */
    private String generateLimitKey(JoinPoint joinPoint, RateLimit rateLimit) {
        RateLimitType type = rateLimit.type();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return switch (type) {
            // 限流用户
            case USER: {
                User loginUser = userService.getLoginUser(request);
                // 根据用户id拼接key
                yield String.format("yuchuang:user:rateLimit:%s", loginUser.getId());
            }
            // 限流IP
            case IP: {
                String clientIP = IpUtil.getClientIP(request);
                yield String.format("yuchuang:ip:rateLimit:%s", clientIP);
            }
            // 限流接口
            case API: {
                String name = joinPoint.getSignature().getName();
                yield String.format("yuchuang:api:rateLimit:%s", name);
            }
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        };
    }
}