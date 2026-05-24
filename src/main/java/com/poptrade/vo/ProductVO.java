package com.poptrade.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品视图 —— 包含分类名称，供前端直接展示。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVO {

    private Long id;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private String img;
    private LocalDateTime createTime;
}
