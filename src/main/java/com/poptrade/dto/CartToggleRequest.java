package com.poptrade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车勾选状态请求。
 */
@Data
public class CartToggleRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "勾选状态不能为空")
    private Boolean checked;
}
