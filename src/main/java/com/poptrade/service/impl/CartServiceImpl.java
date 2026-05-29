package com.poptrade.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poptrade.common.exception.BusinessException;
import com.poptrade.dto.CartItemRequest;
import com.poptrade.entity.Product;
import com.poptrade.mapper.CategoryMapper;
import com.poptrade.mapper.ProductMapper;
import com.poptrade.service.CartService;
import com.poptrade.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Redis 购物车实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final String CART_KEY_PREFIX = "cart:user:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CartItemVO> getCart(Long userId) {
        return loadCart(userId);
    }

    @Override
    public void addItem(Long userId, CartItemRequest request) {
        Product product = requireSellableProduct(request.getProductId());
        List<CartItemVO> cart = loadCart(userId);
        CartItemVO item = findItem(cart, request.getProductId()).orElseGet(() -> {
            CartItemVO vo = new CartItemVO();
            vo.setProductId(product.getId());
            vo.setProductName(product.getProductName());
            vo.setPrice(product.getPrice());
            vo.setCategoryName(resolveCategoryName(product.getCategoryId()));
            vo.setImg(product.getImg());
            vo.setProductNum(0);
            vo.setChecked(true);
            cart.add(vo);
            return vo;
        });
        item.setProductNum(item.getProductNum() + request.getProductNum());
        saveCart(userId, cart);
    }

    @Override
    public void updateCount(Long userId, CartItemRequest request) {
        List<CartItemVO> cart = loadCart(userId);
        CartItemVO item = findItem(cart, request.getProductId())
                .orElseThrow(() -> new BusinessException("购物车商品不存在"));
        item.setProductNum(request.getProductNum());
        saveCart(userId, cart);
    }

    @Override
    public void removeItem(Long userId, Long productId) {
        List<CartItemVO> cart = loadCart(userId);
        cart.removeIf(item -> item.getProductId().equals(productId));
        saveCart(userId, cart);
    }

    @Override
    public void clearCart(Long userId) {
        redisTemplate.delete(key(userId));
    }

    @Override
    public void toggleChecked(Long userId, Long productId, Boolean checked) {
        List<CartItemVO> cart = loadCart(userId);
        findItem(cart, productId).ifPresent(item -> item.setChecked(checked));
        saveCart(userId, cart);
    }

    @Override
    public void toggleAll(Long userId, Boolean checked) {
        List<CartItemVO> cart = loadCart(userId);
        cart.forEach(item -> item.setChecked(checked));
        saveCart(userId, cart);
    }

    @Override
    public List<CartItemVO> getCheckedItems(Long userId) {
        return loadCart(userId).stream()
                .filter(item -> Boolean.TRUE.equals(item.getChecked()))
                .toList();
    }

    @Override
    public void removeItems(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        List<CartItemVO> cart = loadCart(userId);
        cart.removeIf(item -> productIds.contains(item.getProductId()));
        saveCart(userId, cart);
    }

    private List<CartItemVO> loadCart(Long userId) {
        String json = redisTemplate.opsForValue().get(key(userId));
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<CartItemVO>>() {});
        } catch (Exception e) {
            log.warn("读取购物车失败, userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    private void saveCart(Long userId, List<CartItemVO> cart) {
        try {
            if (cart == null || cart.isEmpty()) {
                redisTemplate.delete(key(userId));
                return;
            }
            redisTemplate.opsForValue().set(key(userId), objectMapper.writeValueAsString(cart));
        } catch (Exception e) {
            throw new BusinessException("购物车保存失败");
        }
    }

    private Optional<CartItemVO> findItem(List<CartItemVO> cart, Long productId) {
        return cart.stream().filter(item -> item.getProductId().equals(productId)).findFirst();
    }

    private Product requireSellableProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != Product.STATUS_ON_SHELF) {
            throw new BusinessException("商品已下架");
        }
        return product;
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "";
        }
        return Optional.ofNullable(categoryMapper.selectById(categoryId))
                .map(c -> c.getCategoryName())
                .orElse("");
    }

    private String key(Long userId) {
        return CART_KEY_PREFIX + userId;
    }
}
