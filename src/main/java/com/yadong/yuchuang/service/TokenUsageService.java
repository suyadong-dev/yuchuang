package com.yadong.yuchuang.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.yadong.yuchuang.model.dto.tokenusage.TokenUsageQueryRequest;
import com.yadong.yuchuang.model.entity.TokenUsage;
import com.yadong.yuchuang.model.vo.TokenUsageTopVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Token 使用记录 服务层
 *
 * @author 超人不会飞
 */
public interface TokenUsageService extends IService<TokenUsage> {

    /**
     * 记录Token使用情况
     *
     * @param userId       用户ID
     * @param appId        应用ID
     * @param inputTokens  输入Token数量
     * @param outputTokens 输出Token数量
     * @param callType     调用类型
     * @param content      调用内容
     * @return 记录ID
     */
    Long recordTokenUsage(Long userId, Long appId, Integer inputTokens, Integer outputTokens, String callType,
                          String content);

    /**
     * 获取用户今日已使用的总Token数
     *
     * @param userId 用户ID
     * @return 总Token数
     */
    Integer getTodayTokensByUserId(Long userId);

    /**
     * 获取用户在指定时间范围内的Token使用统计
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    Map<String, Object> getTokenUsageStatsByUserId(Long userId, LocalDateTime startTime, LocalDateTime endTime);


    /**
     * 分页查找用户Token使用记录
     *
     * @param tokenUsageQueryRequest 查询参数
     * @return
     */
    Page<TokenUsage> getTokenUsageList(TokenUsageQueryRequest tokenUsageQueryRequest);

    /**
     * 获取用户Token使用排名前N的记录
     *
     * @return Top N
     */
    List<TokenUsageTopVO> getTokenUsageRankTopN(int n, LocalDateTime startTime, LocalDateTime endTime);
}