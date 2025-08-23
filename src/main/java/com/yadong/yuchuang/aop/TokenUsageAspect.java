package com.yadong.yuchuang.aop;

import com.yadong.yuchuang.annonation.RecordTokenUsage;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.service.TokenUsageService;
import com.yadong.yuchuang.service.UserService;
import com.yadong.yuchuang.utils.TokenCountUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Token 使用记录 切面
 * 用于拦截AI服务调用并记录Token使用情况
 *
 * @author 超人不会飞
 */
@Aspect
@Component
public class TokenUsageAspect {

    @Resource
    private TokenUsageService tokenUsageService;

    @Resource
    private UserService userService;

    /**
     * 定义切点：拦截所有带有RecordTokenUsage注解的方法
     */
    @Pointcut("@annotation(com.yadong.yuchuang.annonation.RecordTokenUsage)")
    public void recordTokenUsagePointcut() {
    }

    /**
     * 环绕通知：在方法执行前后记录Token使用情况
     */
    @Around("recordTokenUsagePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名和参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RecordTokenUsage annotation = method.getAnnotation(RecordTokenUsage.class);
        
        // 获取当前用户
        User loginUser = getCurrentUser();
        if (loginUser == null) {
            // 如果用户未登录，直接执行原方法
            return joinPoint.proceed();
        }

        String methodName = method.getName();
        Object[] args = joinPoint.getArgs();

        // 构建调用内容摘要
        String inputContent = annotation.recordInput() ? buildContentSummary(methodName, args, annotation.maxContentLength()) : "Input content not recorded";
        
        // 估算输入Token数量
        int inputTokenCount = calculateTokenCount(inputContent, annotation.preciseCount());

        try {
            // 执行原方法
            Object result = joinPoint.proceed();

            // 构建输出内容摘要
            String outputContent = annotation.recordOutput() && result != null ? 
                truncateContent(result.toString(), annotation.maxContentLength()) : 
                "Output content not recorded";
            
            // 估算输出Token数量
            int outputTokenCount = calculateTokenCount(outputContent, annotation.preciseCount());

            // 构建完整内容
            String fullContent = inputContent + "\n\nOutput: " + outputTokenCount + " tokens";
            
            // 获取调用类型
            String callType = !annotation.type().isEmpty() ? 
                annotation.type() : 
                method.getDeclaringClass().getSimpleName() + "." + methodName;
            
            // 记录Token使用情况
            tokenUsageService.recordTokenUsage(loginUser.getId(), null, inputTokenCount, outputTokenCount, callType, fullContent);

            return result;
        } catch (Throwable e) {
            // 即使发生异常，也记录Token使用情况
            tokenUsageService.recordTokenUsage(
                loginUser.getId(), 
                null, 
                inputTokenCount, 
                0, 
                "FAILED_" + callType(method, annotation), 
                inputContent + "\nError: " + truncateContent(e.getMessage(), 200)
            );
            throw e;
        }
    }

    /**
     * 获取调用类型
     */
    private String callType(Method method, RecordTokenUsage annotation) {
        return !annotation.type().isEmpty() ? annotation.type() : method.getName();
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return userService.getLoginUser(request);
        } catch (Exception e) {
            // 如果没有请求上下文或用户未登录，返回null
            return null;
        }
    }

    /**
     * 构建调用内容摘要
     */
    private String buildContentSummary(String methodName, Object[] args, int maxLength) {
        StringBuilder summary = new StringBuilder("Method: " + methodName);
        if (args != null && args.length > 0) {
            summary.append("\nArgs: ");
            String argsSummary = Arrays.stream(args)
                    .map(arg -> {
                        if (arg == null) {
                            return "null";
                        }
                        String argStr = arg.toString();
                        // 限制每个参数的长度，避免内容过长
                        return truncateContent(argStr, maxLength / args.length);
                    })
                    .collect(Collectors.joining(", "));
            summary.append(argsSummary);
        }
        return truncateContent(summary.toString(), maxLength);
    }

    /**
     * 截断内容到指定长度
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 计算Token数量
     */
    private int calculateTokenCount(String text, boolean preciseCount) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        if (preciseCount) {
            // 使用精确的Token计数算法
            return TokenCountUtil.estimateTokenCount(text);
        } else {
            // 使用简单估算：每4个字符约等于1个Token
            return (text.length() + 3) / 4;
        }
    }

}