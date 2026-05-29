package com.poptrade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体，映射 chat_message 表。
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    public static final int ROLE_ADMIN = 0;
    public static final int ROLE_CUSTOMER = 1;
    public static final int READ_NO = 0;
    public static final int READ_YES = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long senderId;
    private Integer senderRole;
    private Long receiverId;
    private String content;
    private Integer readStatus;
    private LocalDateTime createTime;
}
