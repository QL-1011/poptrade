package com.poptrade.vo;

import lombok.Data;

@Data
public class AiChatResponseVO {

    private Long sessionId;
    private AiMessageVO userMessage;
    private AiMessageVO assistantMessage;
}
