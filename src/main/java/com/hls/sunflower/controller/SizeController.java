package com.hls.sunflower.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dao.CategoryRepository;
import com.hls.sunflower.dao.SizeRepository;
import com.hls.sunflower.dto.request.SizeRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.SizeResponse;
import com.hls.sunflower.entity.Category;
import com.hls.sunflower.entity.Size;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/sizes")
@RequiredArgsConstructor
@Slf4j
public class SizeController {
    private final SizeRepository sizeRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("")
    public ApiResponse<List<SizeResponse>> listSizes(
            @RequestParam(name = "categoryId", required = false) String categoryId) {
        List<Size> sizes;
        if (categoryId != null && !categoryId.isBlank()) {
            sizes = sizeRepository.findByCategoryId(categoryId);
        } else {
            sizes = sizeRepository.findAll();
        }

        List<SizeResponse> resp = sizes.stream()
                .map(s -> {
                    SizeResponse r = new SizeResponse();
                    r.setId(s.getId());
                    r.setName(s.getName());
                    if (s.getCategory() != null) {
                        r.setCategoryId(s.getCategory().getId());
                        r.setCategoryName(s.getCategory().getName());
                    }
                    return r;
                })
                .collect(Collectors.toList());

        return ApiResponse.<List<SizeResponse>>builder().result(resp).build();
    }

    @GetMapping("/{sizeId}")
    public ApiResponse<SizeResponse> getSize(@PathVariable String sizeId) {
        Size s = sizeRepository.findById(sizeId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        SizeResponse r = SizeResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .categoryId(s.getCategory() != null ? s.getCategory().getId() : null)
                .categoryName(s.getCategory() != null ? s.getCategory().getName() : null)
                .build();
        return ApiResponse.<SizeResponse>builder().result(r).build();
    }

    @PostMapping("")
    public ApiResponse<SizeResponse> createSize(@RequestBody SizeRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        String name = request.getName().trim();
        // try find by name first
        Size existing = sizeRepository.findByName(name).orElse(null);
        if (existing != null) {
            SizeResponse r = SizeResponse.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .categoryId(
                            existing.getCategory() != null
                                    ? existing.getCategory().getId()
                                    : null)
                    .categoryName(
                            existing.getCategory() != null
                                    ? existing.getCategory().getName()
                                    : null)
                    .build();
            return ApiResponse.<SizeResponse>builder().result(r).build();
        }

        Size s = Size.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name(name)
                .build();
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            Category c = categoryRepository.findById(request.getCategoryId()).orElse(null);
            if (c == null) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
            }
            s.setCategory(c);
        }
        sizeRepository.save(s);

        SizeResponse r = SizeResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .categoryId(s.getCategory() != null ? s.getCategory().getId() : null)
                .categoryName(s.getCategory() != null ? s.getCategory().getName() : null)
                .build();

        return ApiResponse.<SizeResponse>builder().result(r).build();
    }
}
