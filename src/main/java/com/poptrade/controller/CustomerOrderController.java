package com.poptrade.controller;

import com.poptrade.common.page.PageResult;
import com.poptrade.common.result.Result;
import com.poptrade.common.util.UserContext;
import com.poptrade.dto.PlaceOrderRequest;
import com.poptrade.service.CustomerOrderService;
import com.poptrade.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 顾客端订单接口。
 */
@Tag(name = "顾客端-订单", description = "下单 + 我的订单")
@RestController
@RequestMapping("/api/customer/order")
@RequiredArgsConstructor
@Validated
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @Operation(summary = "下单")
    @PostMapping
    public Result<OrderVO> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(customerOrderService.placeOrder(userId, request));
    }

    @Operation(summary = "我的订单")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> myOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return Result.success(customerOrderService.myOrders(userId, pageNum, pageSize));
    }

    @Operation(summary = "模拟付款")
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        customerOrderService.payOrder(userId, id);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        customerOrderService.cancelOrder(userId, id);
        return Result.success();
    }
}
