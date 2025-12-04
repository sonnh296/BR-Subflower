package com.hls.sunflower.service;

import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dto.request.BannerRequest;
import com.hls.sunflower.dto.response.BannerResponse;

public interface BannerService {
    BannerResponse createBanner(BannerRequest request);

    BannerResponse updateBanner(String bannerId, BannerRequest request);

    void deleteBanner(String bannerId);

    BannerResponse getActiveBanner();

    BannerResponse uploadBannerImage(String bannerId, MultipartFile image);
}
