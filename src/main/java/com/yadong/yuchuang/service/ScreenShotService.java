package com.yadong.yuchuang.service;

/**
 * 截图服务
 */
public interface ScreenShotService {
    /**
     * 截取网页并上传到COS
     *
     * @param webUrl 网页地址
     * @return 图片的可访问url
     */
    String generateAndUploadScreenshot(String webUrl);
}
