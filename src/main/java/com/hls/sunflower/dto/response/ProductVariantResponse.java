package com.hls.sunflower.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {
    private String id;
    // Expose sizeId and size (name)
    private String sizeId;
    private String size;
    private Double price;
    private Integer stock;
    // New: product option info
    private String productOptionId;
    private String productOptionName;
}
