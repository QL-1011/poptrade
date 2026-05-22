package com.poptrade.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象 —— 返回前端时使用，密码字段不序列化。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO {

    private Long id;
    private String username;

    /** 密码仅内部使用，序列化 JSON 时排除 */
    @JsonIgnore
    private String password;

    private String realName;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
}
