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
public class ProductRequest {
    private String name;
    private String description;
    private List<String> imageUrls;
    private List<ProductVariantRequest> variants;
    private String availableFrom;
    private String availableTo;
    private String categoryId;
    private List<ProductOptionRequest> productOptions;

    // New: accept grouped variants from frontend: each group is a variant type (variantName)
    // and contains multiple size entries. Backward-compatible: this field is optional.
    private List<ProductVariantGroupRequest> variantGroups;
}
