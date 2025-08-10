package com.yadong.yuchuang.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.yadong.yuchuang.manager.CosManager;
import com.yadong.yuchuang.service.ScreenShotService;
import com.yadong.yuchuang.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ScreenShotServiceImpl implements ScreenShotService {
    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 1.截取网页图片到本地
        String localFilePath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        try {
            // 2.上传图片到COS
            String fileName = RandomUtil.randomString(6) + "_compressed.png";
            String key = generateScreenshotKey(FileUtil.getName(localFilePath)) + fileName;
            String url = cosManager.uploadPicture(key, new File(localFilePath));
            log.info("上传图片成功，访问地址为：{}", url);
            return url;
        } finally {
            // 3.删除本地文件
            cleanupLocalFile(localFilePath);
        }
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2025/07/31/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }
}
