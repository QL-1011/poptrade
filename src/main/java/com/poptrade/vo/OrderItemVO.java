package com.poptrade.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单详情中的商品行。
 */
@Data
public class OrderItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer productNum;
    private BigDecimal productPrice;
}
