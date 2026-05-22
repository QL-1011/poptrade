package com.poptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.common.page.PageResult;
import com.poptrade.dto.LoginDTO;
import com.poptrade.dto.UserQueryDTO;
import com.poptrade.dto.UserSaveDTO;
import com.poptrade.entity.User;
import com.poptrade.mapper.UserMapper;
import com.poptrade.service.UserService;
import com.poptrade.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 用户模块核心业务逻辑。
 * <p>密码使用 MD5 加盐前先做一次摘要存储；生产环境应替换为 BCrypt。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    // ======================== 通用 ========================

    @Override
    public UserVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !encryptPassword(dto.getPassword()).equals(user.getPassword())) {
            // 不区分"用户不存在"和"密码错误"，防止撞库
            throw new BusinessException("用户名或密码错误");
        }
        if (User.STATUS_DISABLED == user.getStatus()) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        log.info("用户登录成功: {}", user.getUsername());
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserSaveDTO dto) {
        checkUsernameUnique(dto.getUsername(), null);
        User user = buildUser(dto);
        userMapper.insert(user);
        log.info("顾客注册成功: {}", user.getUsername());
    }

    // ======================== 管理员 CRUD ========================

    @Override
    public UserVO getById(Long id) {
        User user = requireExists(id);
        return toVO(user);
    }

    @Override
    public PageResult<UserVO> page(UserQueryDTO query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(query.getUsername()), User::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getRealName()), User::getRealName, query.getRealName())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                .eq(query.getRole() != null, User::getRole, query.getRole())
                .orderByDesc(User::getCreateTime);

        Page<User> page = userMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return PageResult.from(page, this::toVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(UserSaveDTO dto) {
        checkUsernameUnique(dto.getUsername(), null);
        User user = buildUser(dto);
        userMapper.insert(user);
        log.info("管理员新增用户: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserSaveDTO dto) {
        User user = requireExists(dto.getId());
        checkUsernameUnique(dto.getUsername(), dto.getId());

        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
        log.info("管理员修改用户: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        requireExists(id);
        userMapper.deleteById(id);
        log.info("删除用户: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        userMapper.deleteBatchIds(ids);
        log.info("批量删除用户: ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        User user = requireExists(id);
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("切换用户状态: id={}, status={}", id, status);
    }

    // ======================== 内部工具方法 ========================

    /** Entity → VO */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /** 构建新用户对象，密码加密存储 */
    private User buildUser(UserSaveDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encryptPassword(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole() != null ? dto.getRole() : User.ROLE_CUSTOMER);
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : User.STATUS_ENABLED);
        return user;
    }

    /** 校验用户名唯一性，excludeId 为非 null 时排除自身（修改场景） */
    private void checkUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已被占用");
        }
    }

    /** 查询用户，不存在则抛异常 */
    private User requireExists(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /** MD5 加密 */
    private static String encryptPassword(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
