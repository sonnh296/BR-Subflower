package com.hls.sunflower.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.ProductListResponse;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("")
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        log.debug("Received createProduct request: {}", request);

        return ApiResponse.<ProductResponse>builder()
                .result(productService.addProduct(request))
                .build();
    }

    @GetMapping("")
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @RequestParam(name = "field", required = false, defaultValue = "id") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sort) {
        return ApiResponse.<Page<ProductListResponse>>builder()
                .result(productService.getProducts(field, pageNumber, pageSize, sort))
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getById(productId))
                .build();
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable String productId, @RequestBody ProductRequest request) {
        log.debug("Received updateProduct request for id={} request={}", productId, request);
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, request))
                .build();
    }

    @RequestMapping(value = "/{productId}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder().result("Product has been deleted").build();
    }

    @PostMapping("/{productId}/images")
    public ApiResponse<ProductResponse> uploadProductImages(
            @PathVariable String productId, @RequestParam("images") List<MultipartFile> images) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.uploadProductImages(productId, images))
                .build();
    }

    @PostMapping("/{productId}/images/single")
    public ApiResponse<ProductResponse> addProductImage(
            @PathVariable String productId, @RequestParam("image") MultipartFile image) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.addProductImage(productId, image))
                .build();
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ApiResponse<ProductResponse> removeProductImage(
            @PathVariable String productId, @PathVariable String imageId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.removeProductImage(productId, imageId))
                .build();
    }
}
