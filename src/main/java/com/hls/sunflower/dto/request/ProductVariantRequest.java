package com.hls.sunflower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {
    // Accept sizeId to reference the Size entity
    private String sizeId;
    // Backward-compatible: accept a raw size string. If provided and sizeId is null,
    // the server will create a Size entity with this name.
    private String size;
    private Double price;
    private Integer stock;
    // New: associate this variant to a product option
    private String productOptionId;
    private String productOptionName;
}
