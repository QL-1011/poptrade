package com.poptrade.service;

import com.poptrade.common.page.PageResult;
import com.poptrade.dto.LoginDTO;
import com.poptrade.dto.UserQueryDTO;
import com.poptrade.dto.UserSaveDTO;
import com.poptrade.vo.LoginVO;
import com.poptrade.vo.UserVO;

import java.util.List;

/**
 * 用户模块服务接口。
 */
public interface UserService {

    /** 用户登录，返回 JWT Token 和用户信息 */
    LoginVO login(LoginDTO dto);

    /** 顾客自助注册，role 固定为顾客，密码 Bcyrpt加密 */
    void register(UserSaveDTO dto);

    /** 根据 ID 查询用户 */
    UserVO getById(Long id);

    /** 分页 + 多条件查询 */
    PageResult<UserVO> page(UserQueryDTO query);

    /** 管理员新增用户 */
    void save(UserSaveDTO dto);

    /** 管理员修改用户 */
    void update(UserSaveDTO dto);

    /** 管理员删除用户 */
    void deleteById(Long id);

    /** 管理员批量删除用户 */
    void deleteBatch(List<Long> ids);

    /** 管理员启用/禁用用户 */
    void updateStatus(Long id, Integer status);
}
