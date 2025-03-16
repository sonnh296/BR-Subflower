package com.hls.sunflower.dto.response;

import com.hls.sunflower.entity.ProductItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private String id;

    private int quantity;

    private String addedAt;

    private ProductItem productItem;
}
