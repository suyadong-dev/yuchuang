package com.yadong.yuchuang.controller;

import com.mybatisflex.core.paginate.Page;
import com.yadong.yuchuang.common.BaseResponse;
import com.yadong.yuchuang.common.ResultUtils;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.exception.ThrowUtils;
import com.yadong.yuchuang.model.dto.tokenusage.TokenUsageQueryRequest;
import com.yadong.yuchuang.model.entity.TokenUsage;
import com.yadong.yuchuang.model.vo.TokenUsageTopVO;
import com.yadong.yuchuang.service.TokenUsageService;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Token 使用记录 前端控制器
 *
 * @author 超人不会飞
 */
@RestController
@RequestMapping("/token-usage")
public class TokenUsageController {

    @Resource
    private TokenUsageService tokenUsageService;

    /**
     * 获取用户今日已使用的总Token数
     *
     * @param userId 用户id
     * @return 响应结果
     */
    @GetMapping("/today-total/{userId}")
    public BaseResponse<Integer> getTodayTotalTokens(@PathVariable long userId) {
        ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID错误");
        Integer totalTokens = tokenUsageService.getTodayTokensByUserId(userId);
        return ResultUtils.success(totalTokens);
    }

    /**
     * 获取用户本周Token使用统计
     *
     * @param userId 用户ID
     * @return 响应结果
     */
    @GetMapping("/week-stats/{userId}")
    public BaseResponse<Map<String, Object>> getWeekTokenUsageStats(@PathVariable long userId) {
        ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID错误");
        LocalDateTime startTime = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        LocalDateTime endTime = LocalDateTime.now();
        Map<String, Object> stats = tokenUsageService.getTokenUsageStatsByUserId(userId, startTime, endTime);
        return ResultUtils.success(stats);
    }

    /**
     * 获取用户本月Token使用统计
     *
     * @param userId 用户id
     * @return 响应结果
     */
    @GetMapping("/month-stats/{userId}")
    public BaseResponse<Map<String, Object>> getMonthTokenUsageStats(@PathVariable long userId) {
        ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID错误");
        LocalDateTime startTime = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        LocalDateTime endTime = LocalDateTime.now();
        Map<String, Object> stats = tokenUsageService.getTokenUsageStatsByUserId(userId, startTime, endTime);
        return ResultUtils.success(stats);
    }

    /**
     * 获取用户Token使用记录列表
     *
     * @param request 查询请求类
     * @return 响应结果
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<TokenUsage>> getTokenUsageList(@RequestBody TokenUsageQueryRequest request) {
        // 数据校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "参数错误");
        // 查询分页数据
        Page<TokenUsage> records = tokenUsageService.getTokenUsageList(request);

        return ResultUtils.success(records);
    }

    /**
     * 获取指定时间范围内的Token使用记录和统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 响应结果
     */
    @GetMapping("/range/{userId}")
    public BaseResponse<Map<String, Object>> getTokenUsageByRange(
            @PathVariable long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID错误");

        // 获取统计信息
        Map<String, Object> stats = tokenUsageService.getTokenUsageStatsByUserId(userId, startTime, endTime);

        return ResultUtils.success(stats);
    }

    /**
     * 获取用户Token使用排行榜，Top N
     */
    @GetMapping("/rank/top-n/{n}")
    public BaseResponse<List<TokenUsageTopVO>> getTokenUsageRankTopN(
            @PathVariable int n,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<TokenUsageTopVO> rank = tokenUsageService.getTokenUsageRankTopN(n, startTime, endTime);
        return ResultUtils.success(rank);
    }
}