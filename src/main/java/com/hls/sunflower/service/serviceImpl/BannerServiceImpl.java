package com.hls.sunflower.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dao.BannerRepository;
import com.hls.sunflower.dto.request.BannerRequest;
import com.hls.sunflower.dto.response.BannerResponse;
import com.hls.sunflower.entity.Banner;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.service.AzureBlobStorageService;
import com.hls.sunflower.service.BannerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {
    private final BannerRepository bannerRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    @Override
    @Transactional
    public BannerResponse createBanner(BannerRequest request) {
        Banner banner = Banner.builder()
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .active(request.isActive())
                .build();

        if (banner.isActive()) {
            // Deactivate others
            List<Banner> all = bannerRepository.findAll();
            all.forEach(b -> {
                if (b.isActive()) {
                    b.setActive(false);
                }
            });
            bannerRepository.saveAll(all);
        }

        Banner saved = bannerRepository.save(banner);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(String bannerId, BannerRequest request) {
        Banner banner = bannerRepository
                .findById(bannerId)
                .orElseThrow(() -> new AppException(com.hls.sunflower.exception.ErrorCode.RESOURCE_NOT_FOUND));
        banner.setTitle(request.getTitle());
        banner.setImageUrl(request.getImageUrl());

        if (request.isActive() && !banner.isActive()) {
            // Deactivate others
            List<Banner> all = bannerRepository.findAll();
            all.forEach(b -> {
                if (b.isActive()) b.setActive(false);
            });
            bannerRepository.saveAll(all);
            banner.setActive(true);
        } else if (!request.isActive()) {
            banner.setActive(false);
        }

        Banner saved = bannerRepository.save(banner);
        return toResponse(saved);
    }

    @Override
    public void deleteBanner(String bannerId) {
        if (!bannerRepository.existsById(bannerId)) {
            throw new AppException(com.hls.sunflower.exception.ErrorCode.RESOURCE_NOT_FOUND);
        }
        bannerRepository.deleteById(bannerId);
    }

    @Override
    public BannerResponse getActiveBanner() {
        Banner banner = bannerRepository.findByActiveTrue().orElse(null);
        if (banner == null) return null;
        return toResponse(banner);
    }

    @Override
    @Transactional
    public BannerResponse uploadBannerImage(String bannerId, MultipartFile image) {
        Banner banner = bannerRepository
                .findById(bannerId)
                .orElseThrow(() -> new AppException(com.hls.sunflower.exception.ErrorCode.RESOURCE_NOT_FOUND));
        String url = azureBlobStorageService.uploadProductImage(image, "banners/" + bannerId);
        banner.setImageUrl(url);
        Banner saved = bannerRepository.save(banner);
        return toResponse(saved);
    }

    private BannerResponse toResponse(Banner b) {
        return BannerResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .imageUrl(b.getImageUrl())
                .active(b.isActive())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
