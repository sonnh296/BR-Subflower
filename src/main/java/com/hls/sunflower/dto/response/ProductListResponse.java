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
}
