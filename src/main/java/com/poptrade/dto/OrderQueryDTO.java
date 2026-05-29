package com.poptrade.dto;

import com.poptrade.common.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单分页查询 DTO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends PageRequest {

    /** 订单编号（模糊搜索） */
    private String orderNo;

    /** 订单状态 */
    private Integer status;
}
