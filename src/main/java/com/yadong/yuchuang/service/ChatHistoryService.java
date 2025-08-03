package com.yadong.yuchuang.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yadong.yuchuang.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yadong.yuchuang.model.entity.ChatHistory;
import com.yadong.yuchuang.model.entity.User;
import dev.langchain4j.memory.ChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史表 服务层。
 *
 * @author 超人不会飞
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 分页查询历史聊天记录（用于前端展示）
     */
    Page<ChatHistory> listChatHistoryByPage(long appId, long pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 新增对话记录
     */
    boolean addChatMessage(ChatHistory chatHistory);

    /**
     * 获取查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据appId删除对话记录
     */
    boolean deleteByAppId(Long appId);

    /**
     * 加载对话历史
     */
    int loadChatHistoryToMemory(long appId, ChatMemory chatMemory, int maxCount);
}

