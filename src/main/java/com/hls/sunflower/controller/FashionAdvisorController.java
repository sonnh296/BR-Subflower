package com.hls.sunflower.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dto.request.FashionAdvisorRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.FashionAdvisorResponse;
import com.hls.sunflower.service.FashionAdvisorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/fashion-advisor")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FashionAdvisorController {

    private final FashionAdvisorService fashionAdvisorService;

    @PostMapping("/recommend")
    public ApiResponse<FashionAdvisorResponse> getFashionRecommendation(
            @Valid @RequestBody FashionAdvisorRequest request, HttpServletRequest httpRequest) {

        log.info("Received fashion recommendation request for skin color: {}", request.getSkinColor());

        FashionAdvisorResponse response = fashionAdvisorService.getFashionRecommendation(request, httpRequest);

        return ApiResponse.<FashionAdvisorResponse>builder()
                .code(1000)
                .message("Fashion recommendation generated successfully")
                .result(response)
                .build();
    }

    @GetMapping("/location")
    public ApiResponse<String> getUserLocation(HttpServletRequest httpRequest) {
        String location = fashionAdvisorService.getUserLocationFromRequest(httpRequest);

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Location detected successfully")
                .result(location)
                .build();
    }
}
