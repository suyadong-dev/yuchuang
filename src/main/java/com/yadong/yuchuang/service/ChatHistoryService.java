package com.yadong.yuchuang.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yadong.yuchuang.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yadong.yuchuang.model.entity.ChatHistory;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.vo.ChatHistoryVO;

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
    Page<ChatHistoryVO> listChatHistoryByPage(long appId, long pageSize, LocalDateTime lastCreateTime, User loginUser);

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
}

