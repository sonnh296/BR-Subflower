package com.hls.sunflower.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionAdvisorResponse {
    private String recommendation;
    private String userCountry;
    private String userLocation;
    private List<String> suggestedColors;
    private List<String> suggestedStyles;
    private String bodyType;
    private String additionalTips;
}
