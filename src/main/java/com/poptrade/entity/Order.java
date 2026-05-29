package com.poptrade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，映射 order 表。
 */
@Data
@TableName("`order`")
public class Order {

    /** 状态：待付款 */
    public static final int STATUS_PENDING = 0;
    /** 状态：已付款 */
    public static final int STATUS_PAID = 1;
    /** 状态：已发货 */
    public static final int STATUS_SHIPPED = 2;
    /** 状态：已完成 */
    public static final int STATUS_COMPLETED = 3;
    /** 状态：已取消 */
    public static final int STATUS_CANCELLED = 4;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalPrice;
    private Integer status;
    private LocalDateTime createTime;
}
