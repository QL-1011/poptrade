package com.poptrade.service;

import com.poptrade.common.page.PageResult;
import com.poptrade.dto.ChatMessageRequest;
import com.poptrade.dto.ChatSessionQueryDTO;
import com.poptrade.vo.ChatMessageVO;
import com.poptrade.vo.ChatSessionVO;

public interface ChatService {

    ChatSessionVO getOrCreateUserSession(Long userId);

    PageResult<ChatMessageVO> getUserMessages(Long userId, Integer pageNum, Integer pageSize);

    void sendUserMessage(Long userId, ChatMessageRequest request);

    void markUserRead(Long userId);

    PageResult<ChatSessionVO> getAdminSessions(ChatSessionQueryDTO query);

    PageResult<ChatMessageVO> getAdminMessages(Long sessionId, Integer pageNum, Integer pageSize);

    void sendAdminMessage(Long adminId, Long sessionId, ChatMessageRequest request);

    void markAdminRead(Long sessionId);
}
