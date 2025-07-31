package com.yadong.yuchuang.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.yadong.yuchuang.model.dto.app.*;
import com.yadong.yuchuang.model.entity.App;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

public interface AppService extends IService<App> {

    /**
     * 创建应用
     */
    long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 删除应用（用户）
     */
    boolean deleteApp(Long id, HttpServletRequest request);

    /**
     * 更新应用（用户）
     */
    boolean updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 根据id获取应用详情
     */
    AppVO getAppById(Long id, HttpServletRequest request);

    /**
     * 分页查询用户的应用列表
     */
    Page<AppVO> listMyAppsByPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页查询精选应用列表
     */
    Page<AppVO> listFeaturedAppsByPage(AppQueryRequest appQueryRequest);

    /**
     * 删除应用（管理员）
     */
    boolean adminDeleteApp(Long id);

    /**
     * 更新应用（管理员）
     */
    boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 分页查询应用列表（管理员）
     */
    Page<AppVO> adminListAppsByPage(AppAdminQueryRequest appAdminQueryRequest);

    /**
     * 根据id获取应用详情（管理员）
     */
    AppVO adminGetAppById(Long id);

    /**
     * 通过对话生成代码
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 部署应用
     *
     * @param appId     应用id
     * @param loginUser 登录用户
     * @return 可访问的url路径
     */
    String deployApp(Long appId, User loginUser);
}
