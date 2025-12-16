package com.hls.sunflower.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantGroupRequest {
    private String variantName; // product option / type name
    private List<VariantSizeEntry> sizes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantSizeEntry {
        private String sizeId;
        private String size; // name
        private Double price;
        private Integer stock;
    }
}
