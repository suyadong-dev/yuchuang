package com.yadong.yuchuang.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.exception.ThrowUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

/**
 * 截图工具类（用来给应用生成封面图）
 */
@Slf4j
public class WebScreenshotUtil {

    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = getChromeOptions(width, height);
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 获取 Chrome 浏览器选项
     */
    private static ChromeOptions getChromeOptions(int width, int height) {
        ChromeOptions options = new ChromeOptions();
        // 无头模式
        options.addArguments("--headless");
        // 禁用GPU（在某些环境下避免问题）
        options.addArguments("--disable-gpu");
        // 禁用沙盒模式（Docker环境需要）
        options.addArguments("--no-sandbox");
        // 禁用开发者shm使用
        options.addArguments("--disable-dev-shm-usage");
        // 设置窗口大小
        options.addArguments(String.format("--window-size=%d,%d", width, height));
        // 禁用扩展
        options.addArguments("--disable-extensions");
        // 设置用户代理
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        return options;
    }

    /**
     * 保存图片到文件
     */
    public static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (IORuntimeException e) {
            log.error("保存图片失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存图片失败");
        }
    }

    /**
     * 将图片压缩
     */
    public static void compressImage(String originalImagePath, String compressedImagePath) {
        // 定义一个压缩率
        float COMPRESSION_RATE = 0.3f;
        try {
            // 使用hutool工具类压缩图片
            ImgUtil.compress(new File(originalImagePath), new File(compressedImagePath), COMPRESSION_RATE);
        } catch (IORuntimeException e) {
            log.error("压缩图片失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    /**
     * 截图网页
     *
     * @param webUrl 网页地址
     * @return 图片绝对路径
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 1.参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页地址不能为空");
        // 2.访问网页
        webDriver.get(webUrl);
        // 3.等待页面加载完成
        waitForPageLoad(webDriver);
        // 4.截图
        TakesScreenshot screenshot = (TakesScreenshot) webDriver;
        byte[] imageBytes = screenshot.getScreenshotAs(OutputType.BYTES);
        // 5.创建临时目录
        String rootPath = String.format("%s/temp/screenshots/%s",
                System.getProperty("user.dir"), RandomUtil.randomString(8));
        String originalImagePath = String.format("%s/%s.png", rootPath,RandomUtil.randomString(5));
        // 5.保存图片
        saveImage(imageBytes, originalImagePath);
        log.info("截图成功，保存路径为：{}", originalImagePath);
        // 6.压缩图片
        String compressedImagePath = String.format("%s/compressed_image.png", rootPath);
        compressImage(originalImagePath, compressedImagePath);
        log.info("图片压缩成功，保存路径为：{}", compressedImagePath);
        // 7.返回压缩后的图片路径
        return compressedImagePath;
    }
}
