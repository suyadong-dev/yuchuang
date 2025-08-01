package com.yadong.yuchuang.model.enums;

import lombok.Getter;

/**
 * 聊天消息类型枚举
 */
@Getter
public enum ChatMessageTypeEnum {
    USER("user"),
    AI("ai");

    private final String value;

    ChatMessageTypeEnum(String value) {
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static ChatMessageTypeEnum getEnumByValue(String value) {
        for (ChatMessageTypeEnum anEnum : ChatMessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
