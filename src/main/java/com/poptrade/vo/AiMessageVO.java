package com.poptrade.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiMessageVO {

    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private LocalDateTime createTime;
}
