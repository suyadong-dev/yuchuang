package com.yadong.yuchuang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yadong.yuchuang.mapper.TokenUsageMapper;
import com.yadong.yuchuang.model.dto.tokenusage.TokenUsageQueryRequest;
import com.yadong.yuchuang.model.entity.TokenUsage;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.vo.TokenUsageTopVO;
import com.yadong.yuchuang.service.TokenUsageService;
import com.yadong.yuchuang.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Token 使用记录 服务层实现
 *
 * @author 超人不会飞
 */
@Service
public class TokenUsageServiceImpl extends ServiceImpl<TokenUsageMapper, TokenUsage> implements TokenUsageService {

    @Resource
    private TokenUsageMapper tokenUsageMapper;

    @Resource
    private UserService userService;

    @Override
    public Long recordTokenUsage(Long userId, Long appId, Integer inputTokens, Integer outputTokens, String callType, String content) {
        // 计算总token数
        int totalTokens = inputTokens + outputTokens;

        // 创建记录实体
        TokenUsage tokenUsage = TokenUsage.builder()
                .userId(userId)
                .appId(appId)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(totalTokens)
                .callType(callType)
                .content(content)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();

        // 使用baseMapper插入记录
        tokenUsageMapper.insert(tokenUsage);

        return tokenUsage.getId();
    }

    /**
     * 获取今日用户token消耗数
     */
    @Override
    public Integer getTodayTokensByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("user_id", userId)
                .between("create_time", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0), LocalDateTime.now());
        // 使用自定义SQL方法查询今日总Token数
        List<TokenUsage> list = this.list(queryWrapper);
        return list.stream().mapToInt(TokenUsage::getTotalTokens).sum();
    }

    /**
     * 获取用户在指定时间范围内的Token使用统计
     */
    @Override
    public Map<String, Object> getTokenUsageStatsByUserId(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 构建参数
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        // 使用自定义SQL方法按调用类型分组统计
        List<Map<String, Object>> typeStats = tokenUsageMapper.sumTokensByUserIdAndTimeRange(params);

        // 计算总计
        int totalInputTokens = 0;
        int totalOutputTokens = 0;
        int totalTokens = 0;
        int totalCalls = 0;

        for (Map<String, Object> stat : typeStats) {
            totalInputTokens += ((Number) stat.get("inputTokens")).intValue();
            totalOutputTokens += ((Number) stat.get("outputTokens")).intValue();
            totalTokens += ((Number) stat.get("totalTokens")).intValue();
            totalCalls += ((Number) stat.get("callCount")).intValue();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalInputTokens", totalInputTokens);
        result.put("totalOutputTokens", totalOutputTokens);
        result.put("totalTokens", totalTokens);
        result.put("usageCount", totalCalls);
        result.put("typeStats", typeStats);

        return result;
    }

    /**
     * 分页获取用户的Token使用记录
     */
    @Override
    public Page<TokenUsage> getTokenUsageList(TokenUsageQueryRequest tokenUsageQueryRequest) {
        // 获取查询参数
        Long userId = tokenUsageQueryRequest.getUserId();
        String callType = tokenUsageQueryRequest.getCallType();
        LocalDateTime startTime = tokenUsageQueryRequest.getStartTime();
        LocalDateTime endTime = tokenUsageQueryRequest.getEndTime();
        int pageNum = tokenUsageQueryRequest.getPageNum();
        int pageSize = tokenUsageQueryRequest.getPageSize();

        // 构造查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(TokenUsage::getUserId, userId)
                .eq(TokenUsage::getCallType, callType, StrUtil.isNotBlank(callType))
                .between(TokenUsage::getCreateTime, startTime, endTime)
                .orderBy(TokenUsage::getCreateTime, false)
                .limit(pageNum, pageSize);

        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    /**
     * 获取用户在指定时间范围内的Token使用排名
     * n 如果没有传递默认为10
     * 时间如果没有传递默认为今日
     *
     * @return 排行榜
     */
    @Override
    public List<TokenUsageTopVO> getTokenUsageRankTopN(int n, LocalDateTime startTime, LocalDateTime endTime) {
        // 构建参数
        if (n <= 0) {
            n = 10;
        }
        if (startTime == null) {
            startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            endTime = LocalDateTime.now();
        }
        List<Map<String, Object>> tokenUsageRankTopN = tokenUsageMapper.getTokenUsageRankTopN(n, startTime, endTime);
        return tokenUsageRankTopN.stream()
                .map(item -> {
                    TokenUsageTopVO tokenUsageTopVO = TokenUsageTopVO.builder()
                            .userId((Long) item.get("user_id"))
                            .inputTokens(((Number) item.get("input_tokens")).intValue())
                            .outputTokens(((Number) item.get("output_tokens")).intValue())
                            .totalTokens(((Number) item.get("total_tokens")).intValue())
                            .build();
                    User user = userService.getById(tokenUsageTopVO.getUserId());
                    tokenUsageTopVO.setUser(userService.getUserVO(user));
                    return tokenUsageTopVO;
                }).toList();
    }

}