package com.poptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.common.page.PageResult;
import com.poptrade.dto.OrderQueryDTO;
import com.poptrade.entity.Order;
import com.poptrade.entity.OrderItem;
import com.poptrade.entity.Product;
import com.poptrade.entity.User;
import com.poptrade.mapper.OrderItemMapper;
import com.poptrade.mapper.OrderMapper;
import com.poptrade.mapper.ProductMapper;
import com.poptrade.mapper.UserMapper;
import com.poptrade.service.OrderService;
import com.poptrade.vo.OrderItemVO;
import com.poptrade.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult<OrderVO> page(OrderQueryDTO query) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .like(StringUtils.hasText(query.getOrderNo()),
                        Order::getOrderNo, query.getOrderNo())
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .orderByDesc(Order::getCreateTime);

        Page<Order> page = orderMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return PageResult.from(page, this::toVO);
    }

    @Override
    public OrderVO getById(Long id) {
        Order order = requireExists(id);
        OrderVO vo = toVO(order);
        vo.setItems(loadOrderItems(order.getId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Order order = requireExists(id);
        int current = order.getStatus();
        if (current == Order.STATUS_CANCELLED || current == Order.STATUS_COMPLETED) {
            throw new BusinessException("已结束的订单不可修改状态");
        }
        if (current == status) {
            return;
        }
        // 只允许单向推进或取消
        boolean valid = switch (current) {
            case Order.STATUS_PENDING  -> status == Order.STATUS_PAID || status == Order.STATUS_CANCELLED;
            case Order.STATUS_PAID     -> status == Order.STATUS_SHIPPED;
            case Order.STATUS_SHIPPED  -> status == Order.STATUS_COMPLETED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("不允许的状态变更");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
        log.info("更新订单状态: id={}, {} → {}", id, current, status);
    }

    /** Order → OrderVO，含用户名 */
    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
        }
        return vo;
    }

    /** 加载订单商品明细，含商品名称 */
    private List<OrderItemVO> loadOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        // 批量查商品名称
        Set<Long> productIds = items.stream().map(OrderItem::getProductId).collect(Collectors.toSet());
        Map<Long, String> nameMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Product::getProductName));

        return items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVO.setProductName(nameMap.getOrDefault(item.getProductId(), "已删除"));
            return itemVO;
        }).collect(Collectors.toList());
    }

    private Order requireExists(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }
}
