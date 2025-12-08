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
    // Category info
    private String categoryId;
    private String categoryName;
    // New: product options
    private List<ProductOptionResponse> productOptions;
    // New: product options with their variants grouped for easier frontend rendering
    private List<ProductOptionWithVariantsResponse> optionsWithVariants;
}
