package com.poptrade.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户新增 / 修改请求参数。
 * <p>新增时 id 为空，修改时 id 必填。</p>
 */
@Data
public class UserSaveDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 新增时必填，修改时可不传（不修改密码）。
     */
    private String password;

    private String realName;

    /**
     * 角色：0=管理员，1=顾客。新增时若为空，默认为顾客。
     */
    private Integer role;

    /**
     * 状态：0=禁用，1=启用。新增时若为空，默认为启用。
     */
    private Integer status;
}
