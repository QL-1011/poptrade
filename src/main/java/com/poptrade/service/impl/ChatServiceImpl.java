package com.poptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.common.page.PageResult;
import com.poptrade.dto.ChatMessageRequest;
import com.poptrade.dto.ChatSessionQueryDTO;
import com.poptrade.entity.ChatMessage;
import com.poptrade.entity.ChatSession;
import com.poptrade.entity.User;
import com.poptrade.mapper.ChatMessageMapper;
import com.poptrade.mapper.ChatSessionMapper;
import com.poptrade.mapper.UserMapper;
import com.poptrade.service.ChatService;
import com.poptrade.vo.ChatMessageVO;
import com.poptrade.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionVO getOrCreateUserSession(Long userId) {
        return toSessionVO(getOrCreateSession(userId));
    }

    @Override
    public PageResult<ChatMessageVO> getUserMessages(Long userId, Integer pageNum, Integer pageSize) {
        ChatSession session = getOrCreateSession(userId);
        return getMessages(session.getId(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendUserMessage(Long userId, ChatMessageRequest request) {
        ChatSession session = getOrCreateSession(userId);
        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(userId);
        message.setSenderRole(ChatMessage.ROLE_CUSTOMER);
        message.setReceiverId(session.getAdminId());
        message.setContent(request.getContent());
        message.setReadStatus(ChatMessage.READ_NO);
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);

        session.setLastMessage(request.getContent());
        session.setLastMessageTime(message.getCreateTime());
        session.setAdminUnreadCount(nvl(session.getAdminUnreadCount()) + 1);
        session.setStatus(ChatSession.STATUS_OPEN);
        chatSessionMapper.updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUserRead(Long userId) {
        ChatSession session = getOrCreateSession(userId);
        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, session.getId())
                .eq(ChatMessage::getSenderRole, ChatMessage.ROLE_ADMIN)
                .set(ChatMessage::getReadStatus, ChatMessage.READ_YES));
        session.setUserUnreadCount(0);
        chatSessionMapper.updateById(session);
    }

    @Override
    public PageResult<ChatSessionVO> getAdminSessions(ChatSessionQueryDTO query) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getStatus, ChatSession.STATUS_OPEN)
                .orderByDesc(ChatSession::getLastMessageTime)
                .orderByDesc(ChatSession::getCreateTime);
        Page<ChatSession> page = chatSessionMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<ChatSessionVO> list = page.getRecords().stream()
                .map(this::toSessionVO)
                .filter(vo -> matchKeyword(vo, query.getKeyword()))
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), list, query.getPageNum(), query.getPageSize());
    }

    @Override
    public PageResult<ChatMessageVO> getAdminMessages(Long sessionId, Integer pageNum, Integer pageSize) {
        requireSession(sessionId);
        return getMessages(sessionId, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendAdminMessage(Long adminId, Long sessionId, ChatMessageRequest request) {
        ChatSession session = requireSession(sessionId);
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(adminId);
        message.setSenderRole(ChatMessage.ROLE_ADMIN);
        message.setReceiverId(session.getUserId());
        message.setContent(request.getContent());
        message.setReadStatus(ChatMessage.READ_NO);
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);

        session.setAdminId(adminId);
        session.setLastMessage(request.getContent());
        session.setLastMessageTime(message.getCreateTime());
        session.setUserUnreadCount(nvl(session.getUserUnreadCount()) + 1);
        session.setStatus(ChatSession.STATUS_OPEN);
        chatSessionMapper.updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAdminRead(Long sessionId) {
        ChatSession session = requireSession(sessionId);
        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getSenderRole, ChatMessage.ROLE_CUSTOMER)
                .set(ChatMessage::getReadStatus, ChatMessage.READ_YES));
        session.setAdminUnreadCount(0);
        chatSessionMapper.updateById(session);
    }

    private ChatSession getOrCreateSession(Long userId) {
        ChatSession session = chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .last("limit 1"));
        if (session != null) {
            return session;
        }
        User user = userMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getRole(), User.ROLE_CUSTOMER)) {
            throw new BusinessException("用户不存在");
        }
        session = new ChatSession();
        session.setUserId(userId);
        session.setUserUnreadCount(0);
        session.setAdminUnreadCount(0);
        session.setStatus(ChatSession.STATUS_OPEN);
        session.setCreateTime(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return session;
    }

    private ChatSession requireSession(Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("聊天会话不存在");
        }
        return session;
    }

    private PageResult<ChatMessageVO> getMessages(Long sessionId, Integer pageNum, Integer pageSize) {
        Page<ChatMessage> page = chatMessageMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime)
                        .orderByAsc(ChatMessage::getId));
        return PageResult.from(page, this::toMessageVO);
    }

    private ChatSessionVO toSessionVO(ChatSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        BeanUtils.copyProperties(session, vo);
        User user = userMapper.selectById(session.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        if (session.getAdminId() != null) {
            User admin = userMapper.selectById(session.getAdminId());
            if (admin != null) {
                vo.setAdminName(admin.getUsername());
            }
        }
        return vo;
    }

    private ChatMessageVO toMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getUsername());
        }
        return vo;
    }

    private boolean matchKeyword(ChatSessionVO vo, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String text = keyword.trim();
        return contains(vo.getUsername(), text) || contains(vo.getRealName(), text) || contains(vo.getLastMessage(), text);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }
}
