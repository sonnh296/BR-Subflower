package com.hls.sunflower.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemResponse {
    private String id;

    private double price;

    private String category;

    private String gender;

    private String size;

    private String color;

    private int stockQuantity;

    private boolean isActive;

    private String url;
}
