package com.poptrade.service;

import com.poptrade.dto.AiChatRequest;
import com.poptrade.vo.AiChatResponseVO;
import com.poptrade.vo.AiMessageVO;
import com.poptrade.vo.AiSessionVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiAssistantService {

    List<AiSessionVO> listSessions(Long userId);

    AiSessionVO createSession(Long userId);

    List<AiMessageVO> listMessages(Long userId, Long sessionId);

    AiChatResponseVO sendMessage(Long userId, Long sessionId, AiChatRequest request);

    Flux<String> streamMessage(Long userId, Long sessionId, AiChatRequest request);
}
