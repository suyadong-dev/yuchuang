package com.yadong.yuchuang.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * App 更新请求
 */
@Data
public class AppUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;
}
