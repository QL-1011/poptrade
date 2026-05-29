package com.poptrade.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long adminId;
    private String adminName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer userUnreadCount;
    private Integer adminUnreadCount;
    private Integer status;
    private LocalDateTime createTime;
}
