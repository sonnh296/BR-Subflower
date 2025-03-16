package com.hls.sunflower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemRequest {
    private double price;

    private String category;

    private String gender;

    private String size;

    private String color;

    private int stockQuantity;

    private boolean isActive;

    private String url;
}
