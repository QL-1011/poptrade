package com.poptrade.controller;

import com.poptrade.common.page.PageResult;
import com.poptrade.common.result.Result;
import com.poptrade.common.util.UserContext;
import com.poptrade.dto.ChatMessageRequest;
import com.poptrade.dto.ChatSessionQueryDTO;
import com.poptrade.service.ChatService;
import com.poptrade.vo.ChatMessageVO;
import com.poptrade.vo.ChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "聊天管理", description = "管理员客服会话管理")
@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
@Validated
public class AdminChatController {

    private final ChatService chatService;

    @Operation(summary = "分页查询聊天会话")
    @GetMapping("/sessions")
    public Result<PageResult<ChatSessionVO>> sessions(@Valid ChatSessionQueryDTO query) {
        return Result.success(chatService.getAdminSessions(query));
    }

    @Operation(summary = "查询会话消息")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<PageResult<ChatMessageVO>> messages(@PathVariable Long sessionId,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(chatService.getAdminMessages(sessionId, pageNum, pageSize));
    }

    @Operation(summary = "管理员回复消息")
    @PostMapping("/sessions/{sessionId}/messages")
    public Result<Void> send(@PathVariable Long sessionId, @Valid @RequestBody ChatMessageRequest request) {
        chatService.sendAdminMessage(UserContext.getUserId(), sessionId, request);
        return Result.success();
    }

    @Operation(summary = "标记会话已读")
    @PutMapping("/sessions/{sessionId}/read")
    public Result<Void> read(@PathVariable Long sessionId) {
        chatService.markAdminRead(sessionId);
        return Result.success();
    }
}
