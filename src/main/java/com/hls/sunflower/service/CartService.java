package com.hls.sunflower.service;

import com.hls.sunflower.dto.request.CartItemQuantityRequest;
import com.hls.sunflower.dto.request.CartRequest;
import com.hls.sunflower.dto.response.CartItemResponse;
import com.hls.sunflower.dto.response.CartResponse;
import org.springframework.data.domain.Page;

public interface CartService {
    Page<CartItemResponse> getCartItems(String field, Integer pageNumber, Integer pageSize, String sort);
    CartResponse getById(String id);
    CartResponse addCart(CartRequest request);
    CartItemResponse updateCartItemQuantity(String CartItemId, CartItemQuantityRequest request);
    void deleteCartItem(String cartItemId);
}
