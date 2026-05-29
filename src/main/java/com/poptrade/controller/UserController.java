package com.poptrade.controller;

import com.poptrade.common.page.PageResult;
import com.poptrade.common.result.Result;
import com.poptrade.common.util.UserContext;
import com.poptrade.dto.ChangePasswordDTO;
import com.poptrade.dto.LoginDTO;
import com.poptrade.dto.UserQueryDTO;
import com.poptrade.dto.UserSaveDTO;
import com.poptrade.service.UserService;
import com.poptrade.vo.LoginVO;
import com.poptrade.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户模块接口。
 * <p>通用路径 /api/user/* 对管理员和顾客均开放；管理员专属路径 /api/admin/user/*。</p>
 */
@Tag(name = "用户管理", description = "用户注册、登录、CRUD")
@RestController
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // ==================== 通用接口（管理员 & 顾客） ====================

    @Operation(summary = "用户登录")
    @PostMapping("/api/user/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "顾客注册")
    @PostMapping("/api/user/register")
    public Result<Void> register(@Valid @RequestBody UserSaveDTO dto) {
        dto.setRole(1);
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "修改密码（当前登录用户）")
    @PutMapping("/api/user/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(UserContext.getUserId(), dto);
        return Result.success();
    }

    // ==================== 管理员专属接口 ====================

    @Operation(summary = "新增用户")
    @PostMapping("/api/admin/user")
    public Result<Void> save(@Valid @RequestBody UserSaveDTO dto) {
        userService.save(dto);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @PutMapping("/api/admin/user")
    public Result<Void> update(@Valid @RequestBody UserSaveDTO dto) {
        userService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/api/admin/user/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/api/admin/user/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/api/admin/user/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Parameter(description = "0=禁用, 1=启用") @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/api/admin/user/page")
    public Result<PageResult<UserVO>> page(@Valid UserQueryDTO query) {
        return Result.success(userService.page(query));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/api/admin/user/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}
