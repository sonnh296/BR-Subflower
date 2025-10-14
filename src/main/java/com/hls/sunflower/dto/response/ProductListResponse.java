package com.hls.sunflower.dto.response;

import java.util.Set;

import com.hls.sunflower.dto.request.ProductItemRequest;

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

    private Set<ProductItemRequest> productItem;
}
