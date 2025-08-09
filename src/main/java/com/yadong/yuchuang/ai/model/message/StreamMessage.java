package com.yadong.yuchuang.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {
    /**
     * 消息类型
     */
    public String type;
}
