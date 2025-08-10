package com.yadong.yuchuang.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.exception.ThrowUtils;
import com.yadong.yuchuang.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@Service
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    /**
     * 检查路径是否允许包含在压缩包中
     *
     * @param projectRoot 项目根目录
     * @param fullPath    完整路径
     * @return 是否允许
     */
    private boolean shouldExclude(Path projectRoot, Path fullPath) {
        // 获取相对路径
        Path relativePath = projectRoot.relativize(fullPath);
        // 检查路径中的每一部分
        for (Path part : relativePath) {
            String partName = part.toString();
            // 检查是否在忽略名称列表中
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            // 检查文件扩展名
            if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 下载项目为 ZIP 包
     *
     * @param projectPath         项目根路径
     * @param zipDirName            下载的 ZIP 文件名（如 "my-project.zip"）
     * @param httpServletResponse 响应对象
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String zipDirName, HttpServletResponse httpServletResponse) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(zipDirName), ErrorCode.PARAMS_ERROR, "路径名不能为空");

        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.NOT_FOUND_ERROR, "项目目录不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是一个有效目录");

        // 2.设置 Response 响应头
        httpServletResponse.setContentType("application/zip");
        httpServletResponse.setHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", zipDirName));

        // 3.定义文件过滤器
        FileFilter fileFilter = file -> shouldExclude(projectDir.toPath(), file.toPath());
        // 4.使用 ZipUtil 将过滤后的文件压缩到响应输出流
        try {
            ZipUtil.zip(httpServletResponse.getOutputStream(), StandardCharsets.UTF_8,
                    false, fileFilter, projectDir);
            log.info("项目压缩成功，已下载");
        } catch (Exception e) {
            log.error("项目压缩失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "项目压缩失败");
        }
    }
}