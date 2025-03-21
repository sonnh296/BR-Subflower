package com.hls.sunflower.dto.response;

import com.hls.sunflower.dto.request.ProductItemRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private String id;

    private String name;

    private String description;

    private Set<ProductItemRequest> productItem;
}
