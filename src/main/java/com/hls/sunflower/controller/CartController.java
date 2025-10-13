package com.hls.sunflower.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dto.request.CartItemQuantityRequest;
import com.hls.sunflower.dto.request.CartRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.CartItemResponse;
import com.hls.sunflower.dto.response.CartResponse;
import com.hls.sunflower.service.CartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/items")
    public ApiResponse<Page<CartItemResponse>> getCartItems(
            @RequestParam(name = "field", required = false, defaultValue = "addedAt") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "DESC") String sort) {
        return ApiResponse.<Page<CartItemResponse>>builder()
                .result(cartService.getCartItems(field, pageNumber, pageSize, sort))
                .build();
    }

    @PostMapping("")
    public ApiResponse<CartResponse> addCart(@RequestBody CartRequest request) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addCart(request))
                .build();
    }

    @PatchMapping("/items/{cartItemId}/quantity")
    public ApiResponse<CartItemResponse> updateCartItemQuantity(
            @PathVariable String cartItemId, @RequestBody CartItemQuantityRequest request) {
        ;
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.updateCartItemQuantity(cartItemId, request))
                .build();
    }

    @RequestMapping(value = "/items/{cartItemId}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteCartItem(@PathVariable String cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ApiResponse.<String>builder()
                .result("Cart item has been deleted")
                .build();
    }
}
