package com.yadong.yuchuang.model.dto.chathistory;

import com.yadong.yuchuang.common.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话记录查询请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -1L;
    /**
     * id
     */
    private Long id;

    /**
     * 消息
     */
    private String message;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 上一次查询的最早的记录的时间（作为游标）
     * 本次查询的记录的创建时间必须要比这个早
     */
    private LocalDateTime lastCreateTime;
}
