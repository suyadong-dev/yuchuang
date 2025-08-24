package com.yadong.yuchuang.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监控上下文，用户在同一个线程中传递数据
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext {
    /**
     * 应用id
     */
    private Long appId;

    /**
     * 用户id
     */
    private Long userId;
}
