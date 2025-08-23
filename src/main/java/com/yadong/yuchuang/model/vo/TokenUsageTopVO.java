package com.yadong.yuchuang.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * token 使用排行榜
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageTopVO {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户token输入总量
     */
    Integer inputTokens;

    /**
     * AI给用户token输出总量
     */
    Integer outputTokens;

    /**
     * 用户token总使用总量
     */
    Integer totalTokens;

    /**
     * 用户信息
     */
    private UserVO user;
}
