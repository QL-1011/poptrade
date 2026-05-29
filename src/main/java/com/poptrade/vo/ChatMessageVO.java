package com.poptrade.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;
    private Long sessionId;
    private Long senderId;
    private Integer senderRole;
    private Long receiverId;
    private String content;
    private Integer readStatus;
    private LocalDateTime createTime;
    private String senderName;
}
