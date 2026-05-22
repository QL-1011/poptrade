package com.poptrade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，映射 user 表。
 */
@Data
@TableName("user")
public class User {

    /** 角色：管理员 */
    public static final int ROLE_ADMIN = 0;
    /** 角色：普通顾客 */
    public static final int ROLE_CUSTOMER = 1;
    /** 状态：禁用 */
    public static final int STATUS_DISABLED = 0;
    /** 状态：启用 */
    public static final int STATUS_ENABLED = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
}
