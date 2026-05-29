package com.poptrade.service;

import com.poptrade.dto.CartItemRequest;
import com.poptrade.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务。
 */
public interface CartService {

    List<CartItemVO> getCart(Long userId);

    void addItem(Long userId, CartItemRequest request);

    void updateCount(Long userId, CartItemRequest request);

    void removeItem(Long userId, Long productId);

    void clearCart(Long userId);

    void toggleChecked(Long userId, Long productId, Boolean checked);

    void toggleAll(Long userId, Boolean checked);

    List<CartItemVO> getCheckedItems(Long userId);

    void removeItems(Long userId, List<Long> productIds);
}
