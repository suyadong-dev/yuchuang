package com.yadong.yuchuang.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.mybatisflex.core.BaseMapper;
import com.yadong.yuchuang.model.entity.TokenUsage;
import org.apache.ibatis.annotations.MapKey;

/**
 * AI token 使用记录 Mapper
 *
 * @author 超人不会飞
 */
public interface TokenUsageMapper extends BaseMapper<TokenUsage> {
    /**
     * 按调用类型分组统计用户在指定时间范围内的Token使用情况
     *
     * @param params 包含userId、startTime、endTime的参数Map
     * @return 按调用类型分组的统计结果
     */
    @MapKey("call_type")
    List<Map<String, Object>> sumTokensByUserIdAndTimeRange(Map<String, Object> params);

    /**
     * 分页查询用户Token使用记录
     *
     * @param params 包含userId、startTime、endTime、callType、offset、size的参数Map
     * @return Token使用记录列表
     */
    List<TokenUsage> selectUserTokenUsagePage(Map<String, Object> params);

    /**
     * 统计用户Token使用记录总数
     *
     * @param params 包含userId、startTime、endTime、callType的参数Map
     * @return 记录总数
     */
    int countUserTokenUsage(Map<String, Object> params);

    /**
     * 获取用户Token使用排名前N的记录
     *
     * @param n         排行数
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    List<Map<String, Object>> getTokenUsageRankTopN(int n, LocalDateTime startTime, LocalDateTime endTime);
}