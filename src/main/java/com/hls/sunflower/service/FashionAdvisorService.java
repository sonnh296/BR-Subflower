package com.hls.sunflower.service;

import jakarta.servlet.http.HttpServletRequest;

import com.hls.sunflower.dto.request.FashionAdvisorRequest;
import com.hls.sunflower.dto.response.FashionAdvisorResponse;

public interface FashionAdvisorService {
    FashionAdvisorResponse getFashionRecommendation(FashionAdvisorRequest request, HttpServletRequest httpRequest);

    String getUserLocationFromRequest(HttpServletRequest request);
}
