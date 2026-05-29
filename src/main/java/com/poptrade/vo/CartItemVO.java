package com.poptrade.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车单项视图。
 */
@Data
public class CartItemVO {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private String categoryName;
    private String img;
    private Integer productNum;
    private Boolean checked;
}
