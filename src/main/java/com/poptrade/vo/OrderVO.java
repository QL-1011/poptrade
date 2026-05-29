package com.poptrade.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图 —— 包含用户名和商品明细。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private BigDecimal totalPrice;
    private Integer status;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}
