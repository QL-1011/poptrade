package com.poptrade.controller;

import com.poptrade.common.page.PageResult;
import com.poptrade.common.result.Result;
import com.poptrade.dto.OrderQueryDTO;
import com.poptrade.service.OrderService;
import com.poptrade.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理接口，仅管理员可访问。
 */
@Tag(name = "订单管理", description = "订单查询 + 状态流转")
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(@Valid OrderQueryDTO query) {
        return Result.success(orderService.page(query));
    }

    @Operation(summary = "查询订单详情（含商品明细）")
    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Parameter(description = "0=待付款, 1=已付款, 2=已发货, 3=已完成, 4=已取消") @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return Result.success();
    }
}
