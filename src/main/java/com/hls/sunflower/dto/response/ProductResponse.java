package com.hls.sunflower.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private List<String> imageUrls;
    private List<ProductImageResponse> images; // NEW: Include image IDs
    private List<ProductVariantResponse> variants;
    private String availableFrom;
    private String availableTo;
}
