package com.poptrade.service;

import com.poptrade.common.page.PageResult;
import com.poptrade.dto.OrderQueryDTO;
import com.poptrade.vo.OrderVO;

/**
 * 订单管理服务接口。
 */
public interface OrderService {

    /** 分页 + 订单编号/状态筛选 */
    PageResult<OrderVO> page(OrderQueryDTO query);

    /** 查询订单详情（含商品明细） */
    OrderVO getById(Long id);

    /** 更新订单状态 */
    void updateStatus(Long id, Integer status);
}
