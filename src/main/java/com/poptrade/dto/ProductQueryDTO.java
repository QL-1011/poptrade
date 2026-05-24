package com.poptrade.dto;

import com.poptrade.common.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQueryDTO extends PageRequest {

    private String productName;
    private Long categoryId;
    private Integer status;
}
