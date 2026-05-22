package com.poptrade.dto;

import com.poptrade.common.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询参数，继承分页基类。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageRequest {

    /** 用户名模糊搜索 */
    private String username;
    /** 真实姓名模糊搜索 */
    private String realName;
    /** 状态筛选：0=禁用，1=启用 */
    private Integer status;
    /** 角色筛选：0=管理员，1=顾客 */
    private Integer role;
}
