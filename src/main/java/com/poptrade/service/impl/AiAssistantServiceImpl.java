package com.poptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poptrade.common.config.AiProperties;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.dto.AiChatRequest;
import com.poptrade.entity.AiMessage;
import com.poptrade.entity.AiSession;
import com.poptrade.entity.Category;
import com.poptrade.entity.Product;
import com.poptrade.mapper.AiMessageMapper;
import com.poptrade.mapper.AiSessionMapper;
import com.poptrade.mapper.CategoryMapper;
import com.poptrade.mapper.ProductMapper;
import com.poptrade.service.AiAssistantAgent;
import com.poptrade.service.AiAssistantService;
import com.poptrade.tool.AiAssistantToolContext;
import com.poptrade.vo.AiChatResponseVO;
import com.poptrade.vo.AiMessageVO;
import com.poptrade.vo.AiSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final AiAssistantAgent aiAssistantAgent;
    private final AiProperties aiProperties;

    @Override
    public List<AiSessionVO> listSessions(Long userId) {
        return aiSessionMapper.selectList(new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getUserId, userId)
                        .orderByDesc(AiSession::getLastMessageTime)
                        .orderByDesc(AiSession::getCreateTime))
                .stream()
                .map(this::toSessionVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSessionVO createSession(Long userId) {
        AiSession session = new AiSession();
        session.setUserId(userId);
        session.setTitle("新的对话");
        session.setCreateTime(LocalDateTime.now());
        aiSessionMapper.insert(session);
        return toSessionVO(session);
    }

    @Override
    public List<AiMessageVO> listMessages(Long userId, Long sessionId) {
        requireSession(userId, sessionId);
        return aiMessageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getSessionId, sessionId)
                        .orderByAsc(AiMessage::getCreateTime)
                        .orderByAsc(AiMessage::getId))
                .stream()
                .map(this::toMessageVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatResponseVO sendMessage(Long userId, Long sessionId, AiChatRequest request) {
        AiSession session = requireSession(userId, sessionId);
        AiMessage userMessage = saveMessage(sessionId, userId, AiMessage.ROLE_USER, request.getContent());

        String answer;
        try (AiAssistantToolContext.Scope ignored = AiAssistantToolContext.open(userId)) {
            answer = askAssistant(sessionId, userMessage.getId(), request.getContent())
                    .collectList()
                    .map(chunks -> String.join("", chunks))
                    .block();
        }
        if (!StringUtils.hasText(answer)) {
            throw new BusinessException("AI 服务未返回内容");
        }

        AiMessage assistantMessage = saveMessage(sessionId, userId, AiMessage.ROLE_ASSISTANT, answer);
        updateSessionAfterAnswer(session, request.getContent(), answer, assistantMessage.getCreateTime());

        AiChatResponseVO vo = new AiChatResponseVO();
        vo.setSessionId(sessionId);
        vo.setUserMessage(toMessageVO(userMessage));
        vo.setAssistantMessage(toMessageVO(assistantMessage));
        return vo;
    }

    @Override
    public Flux<String> streamMessage(Long userId, Long sessionId, AiChatRequest request) {
        return Flux.defer(() -> {
            AiAssistantToolContext.Scope toolScope = AiAssistantToolContext.open(userId);
            try {
                AiSession session = requireSession(userId, sessionId);
                AiMessage userMessage = saveMessage(sessionId, userId, AiMessage.ROLE_USER, request.getContent());
                StringBuilder answer = new StringBuilder();
                AtomicBoolean saved = new AtomicBoolean(false);

                return askAssistant(sessionId, userMessage.getId(), request.getContent())
                        .publishOn(Schedulers.boundedElastic())
                        .doOnNext(answer::append)
                        .doOnComplete(() -> saveStreamAnswerIfNeeded(SignalType.ON_COMPLETE, saved, session, userMessage, answer))
                        .doFinally(signalType -> {
                            try {
                                if (signalType == SignalType.CANCEL) {
                                    saveStreamAnswerIfNeeded(signalType, saved, session, userMessage, answer);
                                }
                            } finally {
                                toolScope.close();
                            }
                        });
            } catch (RuntimeException ex) {
                toolScope.close();
                return Flux.error(ex);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Flux<String> askAssistant(Long sessionId, Long currentUserMessageId, String question) {
        validateApiKey();
        return aiAssistantAgent.chat(
                buildProductSummary(),
                buildUserMessageWithHistory(sessionId, currentUserMessageId, question)
        );
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new BusinessException("DeepSeek API Key 未配置");
        }
    }

    private String buildUserMessageWithHistory(Long sessionId, Long currentUserMessageId, String question) {
        StringBuilder sb = new StringBuilder();
        List<AiMessage> history = listRecentMessages(sessionId).stream()
                .filter(message -> !message.getId().equals(currentUserMessageId))
                .toList();
        if (!history.isEmpty()) {
            sb.append("最近对话上下文：\n");
            for (AiMessage message : history) {
                sb.append(AiMessage.ROLE_USER.equals(message.getRole()) ? "用户" : "AI助手")
                        .append("：")
                        .append(message.getContent())
                        .append("\n");
            }
            sb.append("\n");
        }
        sb.append("用户本轮问题：").append(question);
        return sb.toString();
    }

    private void saveStreamAnswerIfNeeded(SignalType signalType, AtomicBoolean saved, AiSession session,
                                          AiMessage userMessage, StringBuilder answer) {
        boolean shouldSave = signalType == SignalType.ON_COMPLETE || signalType == SignalType.CANCEL;
        if (!shouldSave || answer.length() == 0 || !saved.compareAndSet(false, true)) {
            return;
        }
        AiMessage assistantMessage = saveMessage(
                session.getId(),
                userMessage.getUserId(),
                AiMessage.ROLE_ASSISTANT,
                answer.toString()
        );
        updateSessionAfterAnswer(session, userMessage.getContent(), assistantMessage.getContent(), assistantMessage.getCreateTime());
    }

    private AiSession requireSession(Long userId, Long sessionId) {
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("AI 会话不存在");
        }
        return session;
    }

    private AiMessage saveMessage(Long sessionId, Long userId, String role, String content) {
        AiMessage message = new AiMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(message);
        return message;
    }

    private List<AiMessage> listRecentMessages(Long sessionId) {
        return aiMessageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getSessionId, sessionId)
                        .orderByDesc(AiMessage::getCreateTime)
                        .orderByDesc(AiMessage::getId)
                        .last("limit 8"))
                .stream()
                .sorted((a, b) -> {
                    int timeCompare = a.getCreateTime().compareTo(b.getCreateTime());
                    return timeCompare != 0 ? timeCompare : a.getId().compareTo(b.getId());
                })
                .toList();
    }

    private void updateSessionAfterAnswer(AiSession session, String question, String answer, LocalDateTime answerTime) {
        session.setLastMessage(answer.length() > 500 ? answer.substring(0, 500) : answer);
        session.setLastMessageTime(answerTime);
        if (!StringUtils.hasText(session.getTitle()) || "新的对话".equals(session.getTitle())) {
            session.setTitle(question.length() > 20 ? question.substring(0, 20) : question);
        }
        aiSessionMapper.updateById(session);
    }

    private String buildProductSummary() {
        StringBuilder sb = new StringBuilder("平台上架商品摘要：\n");
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Product.STATUS_ON_SHELF)
                .orderByDesc(Product::getStock)
                .last("limit 20"));
        for (Product product : products) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            sb.append("- ").append(product.getProductName())
                    .append("，分类：").append(category == null ? "未知" : category.getCategoryName())
                    .append("，价格：").append(product.getPrice())
                    .append("，库存：").append(product.getStock())
                    .append("\n");
        }
        return sb.toString();
    }

    private AiSessionVO toSessionVO(AiSession session) {
        AiSessionVO vo = new AiSessionVO();
        BeanUtils.copyProperties(session, vo);
        return vo;
    }

    private AiMessageVO toMessageVO(AiMessage message) {
        AiMessageVO vo = new AiMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}
