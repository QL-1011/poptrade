package com.poptrade.service;

import com.poptrade.common.page.PageResult;
import com.poptrade.dto.PlaceOrderRequest;
import com.poptrade.vo.OrderVO;

/**
 * 顾客端订单服务接口。
 */
public interface CustomerOrderService {

    /** 下单 */
    OrderVO placeOrder(Long userId, PlaceOrderRequest request);

    /** 查询我的订单 */
    PageResult<OrderVO> myOrders(Long userId, Integer pageNum, Integer pageSize);

    /** 模拟付款 */
    void payOrder(Long userId, Long orderId);

    /** 取消待付款订单 */
    void cancelOrder(Long userId, Long orderId);
}
