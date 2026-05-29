package com.poptrade.controller;

import com.poptrade.common.result.Result;
import com.poptrade.common.util.UserContext;
import com.poptrade.dto.AiChatRequest;
import com.poptrade.service.AiAssistantService;
import com.poptrade.vo.AiChatResponseVO;
import com.poptrade.vo.AiMessageVO;
import com.poptrade.vo.AiSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI购物助手", description = "顾客端 AI 对话")
@RestController
@RequestMapping("/api/customer/ai")
@RequiredArgsConstructor
@Validated
public class CustomerAiAssistantController {

    private static final String TEXT_STREAM_VALUE = "text/stream;charset=utf-8";
    private static final String STREAM_ERROR_PREFIX = "[POPTRADE_STREAM_ERROR]";

    private final AiAssistantService aiAssistantService;

    @Operation(summary = "查询 AI 会话列表")
    @GetMapping("/sessions")
    public Result<List<AiSessionVO>> sessions() {
        return Result.success(aiAssistantService.listSessions(UserContext.getUserId()));
    }

    @Operation(summary = "创建 AI 会话")
    @PostMapping("/sessions")
    public Result<AiSessionVO> createSession() {
        return Result.success(aiAssistantService.createSession(UserContext.getUserId()));
    }

    @Operation(summary = "查询 AI 会话消息")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AiMessageVO>> messages(@PathVariable Long sessionId) {
        return Result.success(aiAssistantService.listMessages(UserContext.getUserId(), sessionId));
    }

    @Operation(summary = "发送 AI 消息")
    @PostMapping("/sessions/{sessionId}/messages")
    public Result<AiChatResponseVO> send(@PathVariable Long sessionId, @Valid @RequestBody AiChatRequest request) {
        return Result.success(aiAssistantService.sendMessage(UserContext.getUserId(), sessionId, request));
    }

    @Operation(summary = "流式发送 AI 消息")
    @PostMapping(value = "/sessions/{sessionId}/messages/stream", produces = TEXT_STREAM_VALUE)
    public Flux<String> stream(@PathVariable Long sessionId, @Valid @RequestBody AiChatRequest request) {
        Long userId = UserContext.getUserId();
        return aiAssistantService.streamMessage(userId, sessionId, request)
                .onErrorResume(ex -> Flux.just(STREAM_ERROR_PREFIX + streamErrorMessage(ex)));
    }

    private String streamErrorMessage(Throwable ex) {
        return StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "AI 回复失败，请稍后再试";
    }
}
