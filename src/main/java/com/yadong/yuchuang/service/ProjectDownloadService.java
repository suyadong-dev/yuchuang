package com.yadong.yuchuang.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载服务
 */
public interface ProjectDownloadService {

    /**
     * 下载项目
     *
     * @param projectPath 项目路径
     * @param zipDirName  压缩包的包名
     * @param response    响应
     */
    void downloadProjectAsZip(String projectPath, String zipDirName, HttpServletResponse response);
}
