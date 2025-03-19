package com.hls.sunflower.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ApiResponse;
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

        return ApiResponse.<ProductResponse>builder()
                .result(productService.addProduct(request))
                .build();
    }

    @GetMapping("")
    public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(name = "field", required = false, defaultValue = "id") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sort) {
        return ApiResponse.<Page<ProductResponse>>builder()
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
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, request))
                .build();
    }

    @RequestMapping(value = "/{productId}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder().result("Product has been deleted").build();
    }
}
