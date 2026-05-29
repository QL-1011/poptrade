package com.poptrade.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.dto.CartItemRequest;
import com.poptrade.entity.Category;
import com.poptrade.entity.Order;
import com.poptrade.entity.OrderItem;
import com.poptrade.entity.Product;
import com.poptrade.mapper.CategoryMapper;
import com.poptrade.mapper.OrderItemMapper;
import com.poptrade.mapper.OrderMapper;
import com.poptrade.mapper.ProductMapper;
import com.poptrade.service.CartService;
import com.poptrade.vo.CartItemVO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("aiAssistantTools")
@RequiredArgsConstructor
public class AiAssistantTools {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final ObjectMapper objectMapper;

    @Tool(name = "商品搜索工具", value = "根据关键词、分类ID、价格区间查询 PopTrade 已上架商品。用户找商品、筛选预算、要求推荐商品时优先使用。")
    public String searchProducts(
            @P("商品关键词；没有关键词时传空字符串") String keyword,
            @P("分类ID；没有分类限制时传0") Long categoryId,
            @P("最低价格；没有最低价时传0") BigDecimal minPrice,
            @P("最高价格；没有最高价时传0") BigDecimal maxPrice,
            @P("最多返回数量；建议5到10，最大20") Integer limit
    ) {
        int size = clampLimit(limit);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Product.STATUS_ON_SHELF)
                .like(StringUtils.hasText(keyword), Product::getProductName, keyword)
                .eq(categoryId != null && categoryId > 0, Product::getCategoryId, categoryId)
                .ge(isPositive(minPrice), Product::getPrice, minPrice)
                .le(isPositive(maxPrice), Product::getPrice, maxPrice)
                .orderByDesc(Product::getStock)
                .orderByAsc(Product::getId)
                .last("limit " + size);

        List<Product> products = productMapper.selectList(wrapper);
        Map<Long, String> categoryMap = categoryNameMap();
        List<ProductResult> result = products.stream()
                .map(product -> toProductResult(product, categoryMap))
                .toList();
        return toJson(new ToolResult(true, result.isEmpty() ? "没有查询到符合条件的上架商品" : "查询成功", result));
    }

    @Tool(name = "商品详情工具", value = "根据商品ID查询上架商品详情，包括名称、分类、价格和库存。用户追问某个商品详情、价格、库存时使用。")
    public String getProductDetail(@P("商品ID") Long productId) {
        if (productId == null || productId <= 0) {
            return fail("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != Product.STATUS_ON_SHELF) {
            return fail("商品不存在或已下架");
        }
        return toJson(new ToolResult(true, "查询成功", toProductResult(product, categoryNameMap())));
    }

    @Tool(name = "分类查询工具", value = "查询 PopTrade 全部商品分类。用户询问有哪些分类、想按分类浏览时使用。")
    public String listCategories() {
        List<CategoryResult> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId))
                .stream()
                .map(category -> new CategoryResult(category.getId(), category.getCategoryName(), category.getSort()))
                .toList();
        return toJson(new ToolResult(true, categories.isEmpty() ? "暂无分类" : "查询成功", categories));
    }

    @Tool(name = "订单查询工具", value = "查询当前登录用户自己的订单列表。用户询问我的订单、待付款订单、最近订单时使用。")
    public String listMyOrders(
            @P("订单状态；0待付款，1已付款，2已发货，3已完成，4已取消；不限制状态时传-1") Integer status,
            @P("最多返回数量；建议5到10，最大20") Integer limit
    ) {
        Long userId = requireUserId();
        int size = clampLimit(limit);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(isKnownOrderStatus(status), Order::getStatus, status)
                .orderByDesc(Order::getCreateTime)
                .last("limit " + size);

        List<OrderResult> orders = orderMapper.selectList(wrapper).stream()
                .map(this::toOrderResult)
                .toList();
        return toJson(new ToolResult(true, orders.isEmpty() ? "没有查询到订单" : "查询成功", orders));
    }

    @Tool(name = "订单详情工具", value = "根据订单ID查询当前登录用户自己的订单详情和商品明细。用户追问某个订单内容时使用。")
    public String getOrderDetail(@P("订单ID") Long orderId) {
        Long userId = requireUserId();
        if (orderId == null || orderId <= 0) {
            return fail("订单ID不能为空");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            return fail("订单不存在或无权查看");
        }
        return toJson(new ToolResult(true, "查询成功", toOrderResult(order)));
    }

    @Tool(name = "购物车查询工具", value = "查询当前登录用户购物车中的商品。用户询问购物车、已加购商品、购物车总价时使用。")
    public String listCart() {
        Long userId = requireUserId();
        CartResult cart = buildCartResult(userId);
        return toJson(new ToolResult(true, cart.items().isEmpty() ? "购物车为空" : "查询成功", cart));
    }

    @Tool(name = "加入购物车工具", value = "把指定商品加入当前登录用户购物车。仅当用户明确要求加入购物车时使用，不要擅自加购。")
    public String addCartItem(
            @P("商品ID") Long productId,
            @P("加入数量；没有说明时传1") Integer productNum
    ) {
        Long userId = requireUserId();
        if (productId == null || productId <= 0) {
            return fail("商品ID不能为空");
        }
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setProductNum(normalizeCount(productNum));
        try {
            cartService.addItem(userId, request);
            return toJson(new ToolResult(true, "已加入购物车", buildCartResult(userId)));
        } catch (BusinessException ex) {
            return fail(ex.getMessage());
        }
    }

    @Tool(name = "修改购物车数量工具", value = "修改当前登录用户购物车中某个商品的数量。仅当用户明确要求修改数量时使用。")
    public String updateCartItemCount(
            @P("商品ID") Long productId,
            @P("新的商品数量，至少为1") Integer productNum
    ) {
        Long userId = requireUserId();
        if (productId == null || productId <= 0) {
            return fail("商品ID不能为空");
        }
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setProductNum(normalizeCount(productNum));
        try {
            cartService.updateCount(userId, request);
            return toJson(new ToolResult(true, "购物车数量已更新", buildCartResult(userId)));
        } catch (BusinessException ex) {
            return fail(ex.getMessage());
        }
    }

    @Tool(name = "删除购物车商品工具", value = "从当前登录用户购物车删除指定商品。仅当用户明确要求删除时使用。")
    public String removeCartItem(@P("商品ID") Long productId) {
        Long userId = requireUserId();
        if (productId == null || productId <= 0) {
            return fail("商品ID不能为空");
        }
        cartService.removeItem(userId, productId);
        return toJson(new ToolResult(true, "已从购物车删除该商品", buildCartResult(userId)));
    }

    @Tool(name = "切换购物车勾选工具", value = "勾选或取消勾选当前登录用户购物车中的指定商品。仅当用户明确要求勾选状态变化时使用。")
    public String toggleCartItemChecked(
            @P("商品ID") Long productId,
            @P("是否勾选；true表示勾选，false表示取消勾选") Boolean checked
    ) {
        Long userId = requireUserId();
        if (productId == null || productId <= 0) {
            return fail("商品ID不能为空");
        }
        cartService.toggleChecked(userId, productId, Boolean.TRUE.equals(checked));
        return toJson(new ToolResult(true, "购物车勾选状态已更新", buildCartResult(userId)));
    }

    @Tool(name = "全选购物车工具", value = "全选或取消全选当前登录用户购物车。仅当用户明确要求全选或取消全选时使用。")
    public String toggleAllCartItems(@P("是否全选；true表示全选，false表示取消全选") Boolean checked) {
        Long userId = requireUserId();
        cartService.toggleAll(userId, Boolean.TRUE.equals(checked));
        return toJson(new ToolResult(true, "购物车全选状态已更新", buildCartResult(userId)));
    }

    @Tool(name = "清空购物车工具", value = "清空当前登录用户购物车。高风险操作，仅当用户明确要求清空购物车时使用。")
    public String clearCart() {
        Long userId = requireUserId();
        cartService.clearCart(userId);
        return toJson(new ToolResult(true, "购物车已清空", buildCartResult(userId)));
    }

    private Long requireUserId() {
        Long userId = AiAssistantToolContext.getUserId();
        if (userId == null) {
            throw new BusinessException("未获取到当前登录用户，无法使用个人数据工具");
        }
        return userId;
    }

    private CartResult buildCartResult(Long userId) {
        List<CartItemVO> cart = cartService.getCart(userId);
        List<CartItemResult> items = cart.stream()
                .map(item -> new CartItemResult(
                        item.getProductId(),
                        item.getProductName(),
                        item.getCategoryName(),
                        item.getPrice(),
                        item.getProductNum(),
                        item.getChecked(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getProductNum()))
                ))
                .toList();
        int totalCount = items.stream().mapToInt(CartItemResult::productNum).sum();
        BigDecimal totalAmount = items.stream()
                .map(CartItemResult::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResult(items.size(), totalCount, totalAmount, items);
    }

    private OrderResult toOrderResult(Order order) {
        List<OrderItemResult> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId()))
                .stream()
                .map(this::toOrderItemResult)
                .toList();
        return new OrderResult(
                order.getId(),
                order.getOrderNo(),
                order.getTotalPrice(),
                order.getStatus(),
                orderStatusText(order.getStatus()),
                order.getCreateTime() == null ? "" : order.getCreateTime().toString(),
                items
        );
    }

    private OrderItemResult toOrderItemResult(OrderItem item) {
        Product product = productMapper.selectById(item.getProductId());
        return new OrderItemResult(
                item.getProductId(),
                product == null ? "已删除商品" : product.getProductName(),
                item.getProductNum(),
                item.getProductPrice()
        );
    }

    private ProductResult toProductResult(Product product, Map<Long, String> categoryMap) {
        return new ProductResult(
                product.getId(),
                product.getProductName(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryId(),
                categoryMap.getOrDefault(product.getCategoryId(), ""),
                product.getImg()
        );
    }

    private Map<Long, String> categoryNameMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName, (a, b) -> a));
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isKnownOrderStatus(Integer status) {
        return status != null && status >= Order.STATUS_PENDING && status <= Order.STATUS_CANCELLED;
    }

    private int clampLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int normalizeCount(Integer productNum) {
        if (productNum == null || productNum <= 0) {
            return 1;
        }
        return productNum;
    }

    private String orderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case Order.STATUS_PENDING -> "待付款";
            case Order.STATUS_PAID -> "已付款";
            case Order.STATUS_SHIPPED -> "已发货";
            case Order.STATUS_COMPLETED -> "已完成";
            case Order.STATUS_CANCELLED -> "已取消";
            default -> "未知";
        };
    }

    private String fail(String message) {
        return toJson(new ToolResult(false, message, null));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    private record ToolResult(boolean success, String message, Object data) {
    }

    private record ProductResult(Long id, String productName, BigDecimal price, Integer stock,
                                 Long categoryId, String categoryName, String img) {
    }

    private record CategoryResult(Long id, String categoryName, Integer sort) {
    }

    private record OrderResult(Long id, String orderNo, BigDecimal totalPrice, Integer status,
                               String statusText, String createTime, List<OrderItemResult> items) {
    }

    private record OrderItemResult(Long productId, String productName, Integer productNum, BigDecimal productPrice) {
    }

    private record CartResult(int itemKinds, int totalCount, BigDecimal totalAmount, List<CartItemResult> items) {
    }

    private record CartItemResult(Long productId, String productName, String categoryName, BigDecimal price,
                                  Integer productNum, Boolean checked, BigDecimal subtotal) {
    }
}
