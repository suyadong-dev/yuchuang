package com.yadong.yuchuang.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI token 使用记录表
 *
 * @author 超人不会飞
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("token_usage")
public class TokenUsage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 输入token数量
     */
    private Integer inputTokens;

    /**
     * 输出token数量
     */
    private Integer outputTokens;

    /**
     * 总token数量
     */
    private Integer totalTokens;

    /**
     * 调用类型
     */
    private String callType;

    /**
     * 调用内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

}