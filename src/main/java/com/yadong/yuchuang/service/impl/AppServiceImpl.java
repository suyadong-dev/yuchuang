package com.yadong.yuchuang.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yadong.yuchuang.constant.AppConstant;
import com.yadong.yuchuang.core.AiCodeGeneratorFacade;
import com.yadong.yuchuang.core.handler.StreamHandlerExecutor;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.exception.ThrowUtils;
import com.yadong.yuchuang.mapper.AppMapper;
import com.yadong.yuchuang.model.dto.app.*;
import com.yadong.yuchuang.model.entity.App;
import com.yadong.yuchuang.model.entity.ChatHistory;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.enums.ChatMessageTypeEnum;
import com.yadong.yuchuang.model.enums.CodeGenTypeEnum;
import com.yadong.yuchuang.model.enums.UserRoleEnum;
import com.yadong.yuchuang.model.vo.AppVO;
import com.yadong.yuchuang.model.vo.UserVO;
import com.yadong.yuchuang.service.AppService;
import com.yadong.yuchuang.service.ChatHistoryService;
import com.yadong.yuchuang.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    @Lazy
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor steamHandlerExecutor;

    /**
     * 聊天生成代码
     *
     * @param appId     应用id
     * @param message   用户提示词
     * @param loginUser 当前登录用户
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1.查询应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 2.校验用户是否是应用的创建者
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);

        // 3.获取应用类型
        CodeGenTypeEnum appType = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(appType == null, ErrorCode.PARAMS_ERROR, "应用类型错误");

        // 4.添加用户消息到历史记录
        ChatHistory chatHistory = ChatHistory.builder()
                .message(message)
                .messageType(ChatMessageTypeEnum.USER.getValue())
                .userId(loginUser.getId())
                .appId(appId).build();
        boolean save = chatHistoryService.addChatMessage(chatHistory);
        if (!save) {
            log.info("保存用户消息失败");
        }

        // 5. 调用 AI 服务生成代码
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, appId, appType);
        return steamHandlerExecutor.execute(codeStream, chatHistoryService, loginUser, appId, appType);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2.查询应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3.校验用户是否是应用的创建者
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 4.如果没有部署标识，则生成部署标识
        if (StrUtil.isBlank(app.getDeployKey())) {
            app.setDeployKey(RandomUtil.randomString(6));
        }

        // 5.获取源目录和目标目录
        String appType = app.getCodeGenType();
        String sourceDir = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + appType + "_" + app.getId();
        if (!FileUtil.exist(sourceDir) || !FileUtil.isDirectory(sourceDir)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        String targetDir = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + app.getDeployKey();
        // 6.拷贝代码
        try {
            FileUtil.copyContent(new File(sourceDir), new File(targetDir), true);
        } catch (Exception e) {
            log.info("拷贝失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "部署失败");
        }

        // 7.更新数据库
        App newApp = App.builder()
                .id(app.getId())
                .deployKey(app.getDeployKey())
                .deployedTime(LocalDateTime.now())
                .editTime(LocalDateTime.now())
                .build();
        this.getMapper().update(newApp);

        // 8.返回可访问的url路径
        return AppConstant.CODE_DEPLOY_HOST + "/" + app.getDeployKey();
    }

    /**
     * 添加应用
     */
    @Override
    public long addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        // 1. 参数校验
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String initPrompt = appAddRequest.getInitPrompt();
        if (initPrompt == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        User loginUser = userService.getLoginUser(request);
        // 2. 创建App对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 设置app的名称为提示词前12位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // TODO 待删除，临时使用
        app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());
        // 3.生成部署标识
        app.setDeployKey(RandomUtil.randomString(6));
        app.setCreateTime(LocalDateTime.now()); // 设置创建时间
        // 4.写入数据库
        boolean result = this.save(app);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建失败");
        }
        // 5.返回应用id
        return app.getId();
    }

    /**
     * 删除应用
     */
    @Override
    public boolean deleteApp(Long id, HttpServletRequest request) {
        // 1.参数和权限校验
        checkAuthAndParams(id, request);
        // 2.删除数据库记录
        return this.removeById(id);
    }

    private void checkAuthAndParams(Long id, HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 2. 校验应用是否存在
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 3. 仅本人或者管理员删除
        if (!app.getUserId().equals(loginUser.getId()) || !loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    /**
     * 更新应用
     */
    @Override
    public boolean updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        //  1. 参数校验
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.查询应用是否存在
        User loginUser = userService.getLoginUser(request);
        Long id = appUpdateRequest.getId();
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 3. 校验用户是否是应用的创建者
        if (!app.getUserId().equals(loginUser.getId()) || !loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 4. 创建App对象
        App updateApp = new App();
        BeanUtil.copyProperties(appUpdateRequest, updateApp);
        updateApp.setEditTime(LocalDateTime.now());  // 设置更新时间
        // 5. 更新数据库
        return this.updateById(updateApp);
    }

    /**
     * 根据id查询应用
     */
    @Override
    public AppVO getAppById(Long id, HttpServletRequest request) {
        // 1.参数和权限校验
        checkAuthAndParams(id, request);
        // 2.查询数据库
        App app = this.getById(id);
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 3.查询应用创建者
        UserVO userVO = userService.getUserVO(userService.getLoginUser(request));
        appVO.setUser(userVO);
        return appVO;
    }

    /**
     * 获取当前用户的应用列表
     */
    @Override
    public Page<AppVO> listMyAppsByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        // 1.获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 2.构造查询条件
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        queryWrapper.eq("user_id", loginUser.getId());
        // 3.查询数据库
        Page<App> appPage = this.mapper.paginate(Page.of(appQueryRequest.getPageNum(), appQueryRequest.getPageSize()), queryWrapper);
        return convertToAppVOPage(appPage);
    }

    /**
     * 获取精选应用列表
     */
    @Override
    public Page<AppVO> listFeaturedAppsByPage(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        queryWrapper.gt("priority", 0)
                .orderBy("priority", !appQueryRequest.getSortOrder().equals("descend"));
        Page<App> appPage = this.mapper.paginate(Page.of(appQueryRequest.getPageNum(), appQueryRequest.getPageSize()), queryWrapper);
        return convertToAppVOPage(appPage);
    }

    @Override
    public boolean adminDeleteApp(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        return this.updateById(app);
    }

    /**
     * 管理员获取应用列表
     */
    @Override
    public Page<AppVO> adminListAppsByPage(AppAdminQueryRequest appAdminQueryRequest) {
        // 1.构造查询条件
        QueryWrapper queryWrapper = getAdminQueryWrapper(appAdminQueryRequest);
        // 2.查询数据库
        Page<App> appPage = this.mapper.paginate(
                Page.of(appAdminQueryRequest.getPageNum(), appAdminQueryRequest.getPageSize()),
                queryWrapper);
        return convertToAppVOPage(appPage);
    }

    /**
     * 获取应用详情（管理员）
     */
    @Override
    public AppVO adminGetAppById(Long id) {
        // 1.参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.查询应用是否存在
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 3.封装返回结果
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        User user = userService.getById(app.getUserId());
        appVO.setUser(userService.getUserVO(user));
        // 4.返回结果
        return appVO;
    }

    /**
     * 构造查询条件（普通用户）
     */
    private QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .like("app_name", appName)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 构造管理员查询条件
     */
    private QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest appAdminQueryRequest) {
        if (appAdminQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = appAdminQueryRequest.getId();
        String appName = appAdminQueryRequest.getAppName();
        String codeGenType = appAdminQueryRequest.getCodeGenType();
        String deployKey = appAdminQueryRequest.getDeployKey();
        Integer priority = appAdminQueryRequest.getPriority();
        Long userId = appAdminQueryRequest.getUserId();
        String sortField = appAdminQueryRequest.getSortField();
        String sortOrder = appAdminQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("code_gen_type", codeGenType)
                .eq("deploy_key", deployKey)
                .eq("priority", priority)
                .eq("user_id", userId)
                .like("app_name", appName)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 将App分页对象转换为AppVO分页对象
     *
     * @param appPage App分页对象
     * @return AppVO分页对象
     */
    private Page<AppVO> convertToAppVOPage(Page<App> appPage) {
        // 1.创建AppVO分页对象
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        // 2.将App列表转换为AppVO列表
        appVOPage.setRecords(BeanUtil.copyToList(appPage.getRecords(), AppVO.class));
        // 3.关联查询用户信息
        List<AppVO> records = appVOPage.getRecords();
        // 3.1对用户id进行去重
        Set<Long> ids = records.stream()
                .map(AppVO::getId)
                .collect(Collectors.toSet());
        // 3.2将id转为 {id：user}
        Map<Long, UserVO> userVOMap = userService.listByIds(ids)
                .stream()
                .map(user -> userService.getUserVO(user))
                .collect(Collectors.toMap(UserVO::getId, userVO -> userVO));

        // 4. 填充用户信息
        records.forEach(appVO -> appVO.setUser(userVOMap.get(appVO.getUserId())));
        appVOPage.setRecords(records);
        // 5.返回结果
        return appVOPage;
    }

    @Override
    public boolean removeById(Serializable id) {
        // 1.删除应用
        super.removeById(id);
        // 2。删除聊天记录
        return chatHistoryService.deleteByAppId((long) id);
    }
}
