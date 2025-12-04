package com.hls.sunflower.dto.response;

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
    private String thumbnailUrl;
    private Double price;

    // Product information
    private String productId;
    private String productName;

    // Variant information
    private String variantId;
    private String size;
    private Integer availableStock;
}
