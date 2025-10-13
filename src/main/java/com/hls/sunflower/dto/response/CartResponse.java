package com.hls.sunflower.dto.response;

import java.util.Set;

import com.hls.sunflower.entity.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private String id;

    private Users user;

    private Set<CartItemResponse> cartItems;
}
