package com.poptrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品新增/修改参数。
 */
@Data
public class ProductSaveDTO {

    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private Integer status;

    private String img;
}
