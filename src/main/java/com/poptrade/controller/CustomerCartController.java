package com.poptrade.controller;

import com.poptrade.common.result.Result;
import com.poptrade.common.util.UserContext;
import com.poptrade.dto.CartItemRequest;
import com.poptrade.dto.CartToggleRequest;
import com.poptrade.service.CartService;
import com.poptrade.vo.CartItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 顾客端购物车接口。
 */
@Tag(name = "顾客端-购物车", description = "Redis 购物车")
@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
@Validated
public class CustomerCartController {

    private final CartService cartService;

    @Operation(summary = "查询购物车")
    @GetMapping
    public Result<List<CartItemVO>> getCart() {
        return Result.success(cartService.getCart(UserContext.getUserId()));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result<Void> addItem(@Valid @RequestBody CartItemRequest request) {
        cartService.addItem(UserContext.getUserId(), request);
        return Result.success();
    }

    @Operation(summary = "修改数量")
    @PutMapping("/count")
    public Result<Void> updateCount(@Valid @RequestBody CartItemRequest request) {
        cartService.updateCount(UserContext.getUserId(), request);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{productId}")
    public Result<Void> removeItem(@PathVariable Long productId) {
        cartService.removeItem(UserContext.getUserId(), productId);
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping
    public Result<Void> clearCart() {
        cartService.clearCart(UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "切换勾选状态")
    @PutMapping("/checked")
    public Result<Void> toggleChecked(@Valid @RequestBody CartToggleRequest request) {
        cartService.toggleChecked(UserContext.getUserId(), request.getProductId(), request.getChecked());
        return Result.success();
    }

    @Operation(summary = "全选/取消全选")
    @PutMapping("/checked/all")
    public Result<Void> toggleAll(@RequestParam Boolean checked) {
        cartService.toggleAll(UserContext.getUserId(), checked);
        return Result.success();
    }
}
