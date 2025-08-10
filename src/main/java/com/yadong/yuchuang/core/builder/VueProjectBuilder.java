package com.yadong.yuchuang.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 构建项目
     *
     * @param projectDir 项目目录
     * @return 构建是否成功
     */
    public boolean buildProject(File projectDir) {
        // 1.检查项目目录是否存在
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录 {} 不存在", projectDir.getAbsolutePath());
            return false;
        }
        log.info("开始构建项目");
        // 2.执行 npm install
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 失败");
            return false;
        }
        // 3.执行 npm run build
        if (!executeNpmRunBuild(projectDir)) {
            log.error("npm run build 失败");
            return false;
        }
        // 4.验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("项目构建失败，dist 目录未生成");
            return false;
        }
        log.info("项目构建成功, {}", distDir.getAbsolutePath());
        return true;
    }

    /**
     * 使用虚拟先吃异步构建项目
     *
     * @param projectDir 项目目录
     */
    public void buildProjectAsync(File projectDir) {
        Thread.ofVirtual().name("VueProjectBuilder-" + System.currentTimeMillis()).start(() -> {
            try {
                if (!buildProject(projectDir)) {
                    log.error("项目构建失败");
                }
            } catch (Exception e) {
                log.error("Vue 项目构建失败：{}", e.getMessage());
            }
        });
    }

    /**
     * 执行命令通用方法
     *
     * @param projectDir 项目目录
     * @param command    命令
     * @param timeout    超时时间
     * @return 命令是否执行成功
     */
    public boolean executeCommand(File projectDir, String command, long timeout) {
        try {
            // 将命令按照空格分割
            String[] commands = command.split(" ");
            log.info("开始执行命令：{}", command);
            Process commandExec = RuntimeUtil.exec(null, projectDir, commands);
            boolean isSuccess = commandExec.waitFor(timeout, TimeUnit.SECONDS);
            if (!isSuccess) {
                log.error("执行命令超时");
                return false;
            }
            String installResult = RuntimeUtil.getResult(commandExec);
            log.info("执行命令结果：{}", installResult);
            return true;
        } catch (Exception e) {
            log.error("执行命令失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 执行 npm install 命令
     *
     * @param projectDir 项目目录
     */
    public boolean executeNpmInstall(File projectDir) {
        log.info("开始执行 npm install 命令");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 20);
    }

    /**
     * 执行 npm run build 命令
     *
     * @param projectDir 项目目录
     */
    public boolean executeNpmRunBuild(File projectDir) {
        log.info("开始执行 npm run build 命令");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 60);
    }

    /**
     * 构建命令, 根据系统类型对命令进行修改
     *
     * @param baseCommand 基础命令
     * @return 构建后的命令
     */
    public String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 判断当前操作系统是否为 Windows
     */
    public boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase().contains("windows");
    }
}