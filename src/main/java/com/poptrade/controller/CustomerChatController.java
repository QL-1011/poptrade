package com.poptrade.controller;

import com.poptrade.common.page.PageResult;
import com.poptrade.common.result.Result;
import com.poptrade.common.util.UserContext;
import com.poptrade.dto.ChatMessageRequest;
import com.poptrade.service.ChatService;
import com.poptrade.vo.ChatMessageVO;
import com.poptrade.vo.ChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "顾客端-聊天", description = "用户联系客服")
@RestController
@RequestMapping("/api/customer/chat")
@RequiredArgsConstructor
@Validated
public class CustomerChatController {

    private final ChatService chatService;

    @Operation(summary = "获取我的聊天会话")
    @GetMapping("/session")
    public Result<ChatSessionVO> session() {
        return Result.success(chatService.getOrCreateUserSession(UserContext.getUserId()));
    }

    @Operation(summary = "获取我的聊天记录")
    @GetMapping("/messages")
    public Result<PageResult<ChatMessageVO>> messages(@RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(chatService.getUserMessages(UserContext.getUserId(), pageNum, pageSize));
    }

    @Operation(summary = "发送聊天消息")
    @PostMapping("/messages")
    public Result<Void> send(@Valid @RequestBody ChatMessageRequest request) {
        chatService.sendUserMessage(UserContext.getUserId(), request);
        return Result.success();
    }

    @Operation(summary = "标记我的消息已读")
    @PutMapping("/read")
    public Result<Void> read() {
        chatService.markUserRead(UserContext.getUserId());
        return Result.success();
    }
}
