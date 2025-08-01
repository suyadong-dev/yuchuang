package com.yadong.yuchuang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import com.yadong.yuchuang.exception.ThrowUtils;
import com.yadong.yuchuang.mapper.ChatHistoryMapper;
import com.yadong.yuchuang.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yadong.yuchuang.model.entity.App;
import com.yadong.yuchuang.model.entity.ChatHistory;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.vo.ChatHistoryVO;
import com.yadong.yuchuang.service.AppService;
import com.yadong.yuchuang.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史表 服务层实现。
 *
 * @author 超人不会飞
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private AppService appService;

    /**
     * 对话记录分页查询
     *
     * @param appId          appId
     * @param pageSize       页面大小
     * @param lastCreateTime 上次查询的最老的时间
     * @param loginUser      当前登录用户
     * @return 分页结果
     */
    @Override
    public Page<ChatHistoryVO> listChatHistoryByPage(long appId, long pageSize, LocalDateTime lastCreateTime, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2.检查app是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "app不存在");

        // 3.校验用户是否具有权限
        if (!loginUser.getUserRole().equals("admin") && !loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看历史对话");
        }

        // 4.构造查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId)
                .lt("create_time", lastCreateTime, lastCreateTime != null)  // 上次查询的最老的时间要比这次查询最新的时间早
                .orderBy("create_time", false);

        Page<ChatHistory> page = this.page(Page.of(1, pageSize), queryWrapper);

        // 5.将page<ChatHistory>转为page<ChatHistoryVO>
        List<ChatHistoryVO> chatVOList = page.getRecords().stream().map(chatHistory -> {
            ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
            BeanUtils.copyProperties(chatHistory, chatHistoryVO);
            return chatHistoryVO;
        }).toList();
        // 6.返回结果
        return new Page<>(chatVOList,
                page.getPageNumber(),
                page.getPageSize(),
                page.getTotalRow());
    }

    /**
     * 保存对话消息
     *
     * @param chatHistory 对话消息
     * @return 是否保存成功
     */
    @Override
    public boolean addChatMessage(ChatHistory chatHistory) {
        // 1.参数校验
        String message = chatHistory.getMessage();
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "对话不能为空");
        String messageType = chatHistory.getMessageType();
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "对话类型不能为空");
        Long appId = chatHistory.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId不合法");
        Long userId = chatHistory.getUserId();
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户id不合法");

        // 2.保存到数据库
        chatHistory.setCreateTime(LocalDateTime.now());
        return this.save(chatHistory);
    }

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询请求类
     * @return 查询条件
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        Long appId = chatHistoryQueryRequest.getAppId();
        // 上次查询的最老的时间
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, id != null)
                .like("message", message, message != null)
                .eq("message_type", messageType, messageType != null)
                .eq("user_id", userId, userId != null)
                .eq("app_id", appId, appId != null);
        // 游标查询逻辑，如果lastCreateTime不为空，则只查询小于lastCreateTime的数据
        if (lastCreateTime != null) {
            queryWrapper.lt("create_time", lastCreateTime);
        }
        // 排序逻辑
        if (StrUtil.isNotBlank(sortField) && StrUtil.isNotBlank(sortOrder)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按照创建时间倒序
            queryWrapper.orderBy("create_time", false);
        }
        return queryWrapper;
    }

    /**
     * 根据appId删除对话记录
     *
     * @param appId appId
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId不合法");
        return this.remove(QueryWrapper.create().eq("app_id", appId));
    }
}
