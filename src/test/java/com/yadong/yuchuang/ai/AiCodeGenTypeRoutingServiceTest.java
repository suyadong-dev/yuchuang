package com.yadong.yuchuang.ai;

import com.yadong.yuchuang.ai.model.AppProperty;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class AiCodeGenTypeRoutingServiceTest {
    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Test
    void routeCodeGenTypeAndAppName() {
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.getAiCodeGenTypeRoutingService();

        String userPrompt = "做一个简单的个人介绍页面";
        CodeGenTypeEnum result = aiCodeGenTypeRoutingService.routeCodeGenType(userPrompt);
        String appName = aiCodeGenTypeRoutingService.generateAppName(userPrompt);
        log.info("用户需求: {} -> {}", userPrompt, result.getValue());
        log.info("用户需求: {} -> {}", userPrompt, appName);

        userPrompt = "做一个公司官网，需要首页、关于我们、联系我们三个页面";
        result = aiCodeGenTypeRoutingService.routeCodeGenType(userPrompt);
        log.info("用户需求: {} -> {}", userPrompt, result.getValue());
        log.info("用户需求: {} -> {}", userPrompt, appName);

        userPrompt = "做一个电商管理系统，包含用户管理、商品管理、订单管理，需要路由和状态管理";
        result = aiCodeGenTypeRoutingService.routeCodeGenType(userPrompt);
        log.info("用户需求: {} -> {}", userPrompt, result.getValue());
        log.info("用户需求: {} -> {}", userPrompt, appName);
    }

    @Test
    public void testGenerateAppProperty() {
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.getAiCodeGenTypeRoutingService();
        String userPrompt = "做一个电商管理系统，包含用户管理、商品管理、订单管理，需要路由和状态管理";
        AppProperty appProperty = aiCodeGenTypeRoutingService.generateAppProperty(userPrompt);
        System.out.println(appProperty.getAppName());
        System.out.println(appProperty.getCodeGenType());
    }
}