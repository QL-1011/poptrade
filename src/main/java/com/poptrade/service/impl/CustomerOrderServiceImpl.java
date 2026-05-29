package com.poptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.common.page.PageResult;
import com.poptrade.dto.OrderItemRequest;
import com.poptrade.dto.PlaceOrderRequest;
import com.poptrade.entity.*;
import com.poptrade.mapper.*;
import com.poptrade.service.CartService;
import com.poptrade.service.CustomerOrderService;
import com.poptrade.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final CartService cartService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO placeOrder(Long userId, PlaceOrderRequest request) {
        List<OrderItemRequest> items = request.getItems();

        // 批量查商品
        Set<Long> productIds = items.stream().map(OrderItemRequest::getProductId).collect(Collectors.toSet());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        // 计算总价 + 校验库存
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemRequest item : items) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: id=" + item.getProductId());
            }
            if (product.getStatus() != Product.STATUS_ON_SHELF) {
                throw new BusinessException("商品已下架: " + product.getProductName());
            }
            if (product.getStock() < item.getProductNum()) {
                throw new BusinessException("库存不足: " + product.getProductName()
                        + "，当前库存 " + product.getStock());
            }
            totalPrice = totalPrice.add(
                    product.getPrice().multiply(BigDecimal.valueOf(item.getProductNum())));
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.STATUS_PENDING);
        orderMapper.insert(order);

        // 创建订单明细 + 扣减库存
        for (OrderItemRequest item : items) {
            Product product = productMap.get(item.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductNum(item.getProductNum());
            orderItem.setProductPrice(product.getPrice());
            orderItemMapper.insert(orderItem);

            int affectedRows = productMapper.deductStock(item.getProductId(), item.getProductNum());
            if (affectedRows == 0) {
                throw new BusinessException("库存不足或商品已下架: " + product.getProductName());
            }
        }

        cartService.removeItems(userId, items.stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList()));

        log.info("下单成功: orderNo={}, userId={}, total={}, items={}",
                order.getOrderNo(), userId, totalPrice, items.size());

        return toVO(order, products);
    }

    @Override
    public PageResult<OrderVO> myOrders(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        Page<Order> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        return PageResult.from(page, order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            // 加载订单的商品数量
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            vo.setItems(items.stream().map(item -> {
                com.poptrade.vo.OrderItemVO itemVO = new com.poptrade.vo.OrderItemVO();
                BeanUtils.copyProperties(item, itemVO);
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    itemVO.setProductName(product.getProductName());
                }
                return itemVO;
            }).collect(Collectors.toList()));
            return vo;
        });
    }

    private OrderVO toVO(Order order, List<Product> products) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }

        Map<Long, String> nameMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Product::getProductName));

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(item -> {
            com.poptrade.vo.OrderItemVO itemVO = new com.poptrade.vo.OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVO.setProductName(nameMap.getOrDefault(item.getProductId(), "已删除"));
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return date + random;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (order.getStatus() != Order.STATUS_PENDING) {
            throw new BusinessException("订单状态不允许付款");
        }
        order.setStatus(Order.STATUS_PAID);
        orderMapper.updateById(order);
        log.info("模拟付款成功: orderId={}, userId={}", orderId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (order.getStatus() != Order.STATUS_PENDING) {
            throw new BusinessException("只有待付款订单可以取消");
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : orderItems) {
            productMapper.restoreStock(item.getProductId(), item.getProductNum());
        }

        order.setStatus(Order.STATUS_CANCELLED);
        orderMapper.updateById(order);
        log.info("取消订单成功: orderId={}, userId={}", orderId, userId);
    }
}
