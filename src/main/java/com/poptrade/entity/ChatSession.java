package com.poptrade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体，映射 chat_session 表。
 */
@Data
@TableName("chat_session")
public class ChatSession {

    public static final int STATUS_OPEN = 1;
    public static final int STATUS_CLOSED = 0;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long adminId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer userUnreadCount;
    private Integer adminUnreadCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
