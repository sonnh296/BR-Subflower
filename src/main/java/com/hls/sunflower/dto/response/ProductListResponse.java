package com.hls.sunflower.dto.response;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {
    private String id;

    private String name;

    private String description;

    private String thumbnailUrl; // Only one image for list view

    private List<String> imageUrls; // Add full list of image URLs

    private List<ProductVariantResponse> variants; // Add variants for edit functionality

    private Set<ProductItemResponse> productItem;

    // Category info for list responses
    private String categoryId;
    private String categoryName;

    // New: include product options in list response to support frontend
    private List<ProductOptionResponse> productOptions;

    // New: options with grouped variants for list responses as well
    private List<ProductOptionWithVariantsResponse> optionsWithVariants;
}
