package com.yadong.yuchuang.controller;

import com.mybatisflex.core.paginate.Page;
import com.yadong.yuchuang.annonation.AuthCheck;
import com.yadong.yuchuang.common.BaseResponse;
import com.yadong.yuchuang.common.DeleteRequest;
import com.yadong.yuchuang.common.ResultUtils;
import com.yadong.yuchuang.common.UserConstant;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.model.dto.app.*;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.vo.AppVO;
import com.yadong.yuchuang.service.AppService;
import com.yadong.yuchuang.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatToGenCode(@RequestParam Long appId, @RequestParam String message,
                                      HttpServletRequest request) {
        // 1.参数校验
        if (appId <= 0 || message.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        return appService.chatToGenCode(appId, message, loginUser);
    }

    /**
     * 创建应用
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = appService.addApp(appAddRequest, request);
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
    @PostMapping("/list/my")
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
    @PostMapping("/admin/list")
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
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> adminGetAppById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AppVO appVO = appService.adminGetAppById(id);
        return ResultUtils.success(appVO);
    }
}
