package com.yadong.yuchuang.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import com.yadong.yuchuang.model.enums.ChatMessageTypeEnum;
import com.yadong.yuchuang.service.AppService;
import com.yadong.yuchuang.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史表 服务层实现。
 *
 * @author 超人不会飞
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private AppService appService;


    /**
     * 加载对话记录到内存
     *
     * @param appId      appId
     * @param chatMemory 每个app独有的缓存模型
     * @param maxCount   最大加载数量
     * @return 加载数量
     */
    @Override
    public int loadChatHistoryToMemory(long appId, ChatMemory chatMemory, int maxCount) {
        try {
            // 1.参数校验
            ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR);
            // 2.构造查询条件
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            // 3.查询历史消息
            List<ChatHistory> chatHistories = this.list(queryWrapper);
            if (CollUtil.isEmpty(chatHistories)) {
                return 0;
            }
            // 4.翻转结果
            chatHistories = chatHistories.reversed();
            int count = 0;
            chatMemory.clear(); // 清空缓存, 防止缓存中有其他对话
            // 5.将结果添加到缓存模型中
            for (ChatHistory chatHistory : chatHistories) {
                // 判断消息类型
                // 5.1用户消息
                if (chatHistory.getMessage().equals(ChatMessageTypeEnum.AI.getValue())) {
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                } else {
                    // 5.2AI消息
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                }
                count += 1;
            }
            log.info("加载历史消息成功，共加载{}条", count);
            return count;
        } catch (Exception e) {
            log.error("加载历史消息失败：{}", e.getMessage());
            // 6.加载失败不影响运行，只是没有对话记忆，返回0
            return 0;
        }
    }

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
    public Page<ChatHistory> listChatHistoryByPage(long appId, long pageSize, LocalDateTime lastCreateTime, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2.检查app是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "app不存在");

        // 3.构造查询条件
        // 按照时间降序是因为要查询的是 <= lastCreateTime 并且离现在的时间最近的记录
        // 正序的查出来的结果是按时间升序的，第一条离当前时间最远，而limit是从第一条开始取，所以需要倒序
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .lt(ChatHistory::getCreateTime, lastCreateTime, lastCreateTime != null)  // 上次查询的最老的时间要比这次查询最新的时间早
                .orderBy(ChatHistory::getCreateTime, false);

        return this.page(Page.of(1, pageSize), queryWrapper);
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
        // 获取查询参数
        Long appId = chatHistoryQueryRequest.getAppId();
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 构造查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getId, id, id != null)
                .like(ChatHistory::getMessage, message, message != null)
                .eq(ChatHistory::getMessageType, messageType, messageType != null)
                .eq(ChatHistory::getUserId, userId, userId != null)
                .eq(ChatHistory::getAppId, appId, appId != null);
        // 游标查询逻辑，如果lastCreateTime不为空，则只查询小于lastCreateTime的数据
        if (lastCreateTime != null) {
            queryWrapper.lt(ChatHistory::getCreateTime, lastCreateTime);
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
