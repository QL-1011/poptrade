package com.poptrade.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiSessionVO {

    private Long id;
    private String title;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createTime;
}
