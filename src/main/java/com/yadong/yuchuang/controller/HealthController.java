package com.yadong.yuchuang.controller;

import com.yadong.yuchuang.annonation.AuthCheck;
import com.yadong.yuchuang.common.BaseResponse;
import com.yadong.yuchuang.common.ResultUtils;
import com.yadong.yuchuang.constant.UserConstant;
import com.yadong.yuchuang.ratelimiter.annotation.RateLimit;
import com.yadong.yuchuang.ratelimiter.enums.RateLimitType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检测
     */
    @GetMapping
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }

    @GetMapping("/test")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @RateLimit(rate = 1, rateInterval = 60, type = RateLimitType.API)
    public BaseResponse<String> test() {
        return ResultUtils.success("ok");
    }
}
