package com.yadong.yuchuang.controller;

import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.yadong.yuchuang.annonation.AuthCheck;
import com.yadong.yuchuang.common.BaseResponse;
import com.yadong.yuchuang.common.DeleteRequest;
import com.yadong.yuchuang.common.ResultUtils;
import com.yadong.yuchuang.constant.UserConstant;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.exception.ThrowUtils;
import com.yadong.yuchuang.model.dto.app.*;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.vo.AppVO;
import com.yadong.yuchuang.ratelimiter.annotation.RateLimit;
import com.yadong.yuchuang.ratelimiter.enums.RateLimitType;
import com.yadong.yuchuang.service.AppService;
import com.yadong.yuchuang.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(rate = 5, rateInterval = 60, type = RateLimitType.USER)
    public Flux<ServerSentEvent<String>> chatToGenCode(
            @RequestParam Long appId,
            @RequestParam String message,
            HttpServletRequest request) {
        // 1.参数校验
        if (appId <= 0 || message.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 3.调用服务并转换为 ServerSentEvent
        Flux<ServerSentEvent<String>> dataStream = appService.chatToGenCode(appId, message, loginUser)
                .map(data -> {
                    Map<String, String> wrapper = new HashMap<>();
                    wrapper.put("d", data);
                    return ServerSentEvent.<String>builder()
//                            .event("code-generation")
                            .data(JSONUtil.toJsonStr(wrapper))
                            .build();
                });
        // 4.创建完成事件
        ServerSentEvent<String> completeEvent = ServerSentEvent.<String>builder()
                .event("done")
                .data("数据流已结束")
                .build();

        // 5.将数据流和完成事件合并
        return dataStream.concatWithValues(completeEvent);
    }


    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        // 参数校验
        if (appDeployRequest == null || appDeployRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务
        String result = appService.deployApp(appDeployRequest.getAppId(), loginUser);
        // 返回结果
        return ResultUtils.success(result);
    }

    /**
     * 下载项目代码
     *
     * @param appId    应用id
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public void downloadProjectCode(@PathVariable long appId, HttpServletRequest request,
                                    HttpServletResponse response) {
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        appService.downloadAppCode(appId, loginUser, response);
    }

    /**
     * 创建应用
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long result = appService.addApp(appAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除应用（用户）
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.deleteApp(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 更新应用（用户）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.updateApp(appUpdateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AppVO appVO = appService.getAppById(id, request);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页查询用户的应用列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppsByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 限制每页最多20条
        if (appQueryRequest.getPageSize() > 20) {
            appQueryRequest.setPageSize(20);
        }
        Page<AppVO> appVOPage = appService.listMyAppsByPage(appQueryRequest, request);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询精选应用列表
     */
    @PostMapping("/list/featured")
    @Cacheable(value = "featured_app_page",
            key = "T(com.yadong.yuchuang.utils.CacheUtil).getCacheKey(#appQueryRequest)",
            condition = "#appQueryRequest.pageNum <= 10")
    public BaseResponse<Page<AppVO>> listFeaturedAppsByPage(@RequestBody AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 限制每页最多20条
        if (appQueryRequest.getPageSize() > 20) {
            appQueryRequest.setPageSize(20);
        }
        Page<AppVO> appVOPage = appService.listFeaturedAppsByPage(appQueryRequest);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 删除应用（管理员）
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.adminDeleteApp(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新应用（管理员）
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.adminUpdateApp(appAdminUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询应用列表（管理员）
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> adminListAppsByPage(@RequestBody AppAdminQueryRequest appAdminQueryRequest) {
        if (appAdminQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<AppVO> appVOPage = appService.adminListAppsByPage(appAdminQueryRequest);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 根据id获取应用详情（管理员）
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> adminGetAppById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AppVO appVO = appService.adminGetAppById(id);
        return ResultUtils.success(appVO);
    }
}
