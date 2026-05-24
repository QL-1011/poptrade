package com.poptrade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体，映射 product 表。
 */
@Data
@TableName("product")
public class Product {

    /** 状态：下架 */
    public static final int STATUS_OFF_SHELF = 0;
    /** 状态：上架 */
    public static final int STATUS_ON_SHELF = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private Integer status;
    private String img;
    private LocalDateTime createTime;
}
