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
public class ProductOptionWithVariantsResponse {
    private String optionId;
    private String optionName;
    private List<ProductVariantResponse> variants;
}
