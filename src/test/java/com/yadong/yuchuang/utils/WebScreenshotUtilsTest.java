package com.yadong.yuchuang.utils;

import com.yadong.yuchuang.manager.CosManager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class WebScreenshotUtilsTest {
    @Resource
    private CosManager cosManager;

    @Test
    public void testScreenshot() {
        String imagePath = WebScreenshotUtils.saveWebPageScreenshot("https://www.baidu.com");
        Assertions.assertNotNull(imagePath);
        // 上传图片到COS
        String url = cosManager.uploadPicture("test.png", new File(imagePath));
        System.out.println( url);
    }
}