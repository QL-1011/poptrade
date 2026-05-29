package com.poptrade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poptrade.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单详情 Mapper。
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
