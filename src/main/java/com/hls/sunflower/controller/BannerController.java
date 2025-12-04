package com.hls.sunflower.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dto.request.BannerRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.BannerResponse;
import com.hls.sunflower.service.BannerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {
    private final BannerService bannerService;

    @GetMapping("/active")
    public ApiResponse<BannerResponse> getActiveBanner() {
        return ApiResponse.<BannerResponse>builder()
                .result(bannerService.getActiveBanner())
                .build();
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<BannerResponse> createBanner(@RequestBody BannerRequest request) {
        return ApiResponse.<BannerResponse>builder()
                .result(bannerService.createBanner(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<BannerResponse> updateBanner(@PathVariable String id, @RequestBody BannerRequest request) {
        return ApiResponse.<BannerResponse>builder()
                .result(bannerService.updateBanner(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<String> deleteBanner(@PathVariable String id) {
        bannerService.deleteBanner(id);
        return ApiResponse.<String>builder().result("Banner deleted").build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<BannerResponse> uploadBannerImage(
            @PathVariable String id, @RequestParam("image") MultipartFile image) {
        // Delegate upload to BannerService which handles Azure storage and updates the Banner record
        return ApiResponse.<BannerResponse>builder()
                .result(bannerService.uploadBannerImage(id, image))
                .build();
    }
}
