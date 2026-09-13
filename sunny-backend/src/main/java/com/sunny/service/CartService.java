package com.sunny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunny.dto.CartDTO;
import com.sunny.dto.CartVO;
import com.sunny.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {

    List<CartVO> getCartList(Long userId);

    void addToCart(Long userId, CartDTO dto);

    void updateQuantity(Long userId, Long productId, Integer quantity);

    void changeQuantity(Long userId, Long productId, Integer delta);

    void deleteCartItem(Long userId, Long productId);

    void deleteById(Long userId, Long cartId);

    void clearCart(Long userId);

    Integer getCartCount(Long userId);
}
