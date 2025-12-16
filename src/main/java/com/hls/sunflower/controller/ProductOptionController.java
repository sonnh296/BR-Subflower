// ...new file...
package com.hls.sunflower.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dao.ProductOptionRepository;
import com.hls.sunflower.dao.ProductRepository;
import com.hls.sunflower.dao.SizeRepository;
import com.hls.sunflower.dto.request.ProductOptionRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.ProductOptionResponse;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductOption;
import com.hls.sunflower.entity.Size;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products/{productId}/options")
@RequiredArgsConstructor
@Slf4j
public class ProductOptionController {
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final SizeRepository sizeRepository;

    @GetMapping("")
    public ApiResponse<List<ProductOptionResponse>> listOptions(@PathVariable String productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        List<ProductOptionResponse> resp = product.getProductOptions().stream()
                .map(opt -> {
                    ProductOptionResponse r = new ProductOptionResponse();
                    r.setId(opt.getId());
                    r.setName(opt.getName());
                    r.setSizeIds(opt.getSizes().stream().map(Size::getId).collect(Collectors.toList()));
                    r.setSizeNames(opt.getSizes().stream().map(Size::getName).collect(Collectors.toList()));
                    return r;
                })
                .collect(Collectors.toList());

        return ApiResponse.<List<ProductOptionResponse>>builder().result(resp).build();
    }

    @PostMapping("")
    public ApiResponse<ProductOptionResponse> createOption(
            @PathVariable String productId, @RequestBody ProductOptionRequest request) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        ProductOption opt = ProductOption.builder()
                .id(
                        request.getId() != null
                                ? request.getId()
                                : java.util.UUID.randomUUID().toString())
                .name(request.getName())
                .product(product)
                .build();

        // resolve size ids
        if (request.getSizeIds() != null) {
            for (String sid : request.getSizeIds()) {
                Size s = sizeRepository.findById(sid).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                opt.getSizes().add(s);
            }
        }
        // resolve size names (create if missing)
        if (request.getSizes() != null) {
            for (String sname : request.getSizes()) {
                if (sname == null || sname.isBlank()) continue;
                String name = sname.trim();
                Size s = sizeRepository.findByName(name).orElse(null);
                if (s == null) {
                    s = Size.builder()
                            .id(java.util.UUID.randomUUID().toString())
                            .name(name)
                            .build();
                    sizeRepository.save(s);
                }
                opt.getSizes().add(s);
            }
        }

        product.getProductOptions().add(opt);
        productRepository.save(product);

        ProductOptionResponse r = new ProductOptionResponse();
        r.setId(opt.getId());
        r.setName(opt.getName());
        r.setSizeIds(opt.getSizes().stream().map(Size::getId).collect(Collectors.toList()));
        r.setSizeNames(opt.getSizes().stream().map(Size::getName).collect(Collectors.toList()));

        return ApiResponse.<ProductOptionResponse>builder().result(r).build();
    }

    @PutMapping("/{optionId}")
    public ApiResponse<ProductOptionResponse> updateOption(
            @PathVariable String productId, @PathVariable String optionId, @RequestBody ProductOptionRequest request) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        ProductOption existing = product.getProductOptions().stream()
                .filter(o -> optionId.equals(o.getId()))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            existing = productOptionRepository
                    .findById(optionId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            // ensure belongs to product
            if (existing.getProduct() == null
                    || !productId.equals(existing.getProduct().getId())) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
            }
        }

        if (request.getName() != null) existing.setName(request.getName());

        if (request.getSizeIds() != null && !request.getSizeIds().isEmpty()) {
            existing.getSizes().clear();
            for (String sid : request.getSizeIds()) {
                Size s = sizeRepository.findById(sid).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                existing.getSizes().add(s);
            }
        }
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            for (String sname : request.getSizes()) {
                if (sname == null || sname.isBlank()) continue;
                String name = sname.trim();
                Size s = sizeRepository.findByName(name).orElse(null);
                if (s == null) {
                    s = Size.builder()
                            .id(java.util.UUID.randomUUID().toString())
                            .name(name)
                            .build();
                    sizeRepository.save(s);
                }
                if (!existing.getSizes().contains(s)) existing.getSizes().add(s);
            }
        }

        productOptionRepository.save(existing);

        ProductOptionResponse r = new ProductOptionResponse();
        r.setId(existing.getId());
        r.setName(existing.getName());
        r.setSizeIds(existing.getSizes().stream().map(Size::getId).collect(Collectors.toList()));
        r.setSizeNames(existing.getSizes().stream().map(Size::getName).collect(Collectors.toList()));

        return ApiResponse.<ProductOptionResponse>builder().result(r).build();
    }

    @DeleteMapping("/{optionId}")
    public ApiResponse<String> deleteOption(@PathVariable String productId, @PathVariable String optionId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        ProductOption existing = product.getProductOptions().stream()
                .filter(o -> optionId.equals(o.getId()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            product.getProductOptions().remove(existing);
            productRepository.save(product);
            return ApiResponse.<String>builder().result("Option removed").build();
        }
        // fallback: try repository delete
        ProductOption repoOpt = productOptionRepository.findById(optionId).orElse(null);
        if (repoOpt != null) {
            if (repoOpt.getProduct() != null
                    && !productId.equals(repoOpt.getProduct().getId())) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
            }
            productOptionRepository.deleteById(optionId);
            return ApiResponse.<String>builder().result("Option removed").build();
        }

        throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
