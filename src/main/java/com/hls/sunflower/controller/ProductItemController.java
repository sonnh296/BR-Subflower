package com.hls.sunflower.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dto.request.ProductItemRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.ProductItemResponse;
import com.hls.sunflower.service.ProductItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/product-items")
@Slf4j
@RequiredArgsConstructor
public class ProductItemController {
    private final ProductItemService productItemService;

    @GetMapping("")
    public ApiResponse<Page<ProductItemResponse>> getProductItems(
            @RequestParam(name = "field", required = false, defaultValue = "id") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sort,
            @RequestParam(name = "productId", required = false) String productId) {
        return ApiResponse.<Page<ProductItemResponse>>builder()
                .result(productItemService.getProductItemList(field, pageNumber, pageSize, sort, productId))
                .build();
    }

    @GetMapping("/{productItemId}")
    public ApiResponse<ProductItemResponse> getProduct(@PathVariable String productItemId) {
        return ApiResponse.<ProductItemResponse>builder()
                .result(productItemService.getById(productItemId))
                .build();
    }

    @PutMapping("/{productItemId}")
    public ApiResponse<ProductItemResponse> updateProductItem(
            @PathVariable String productItemId, @RequestBody ProductItemRequest request) {
        return ApiResponse.<ProductItemResponse>builder()
                .result(productItemService.updateProductItem(productItemId, request))
                .build();
    }

    @PostMapping
    public ApiResponse<ProductItemResponse> addProductItem(
            @RequestBody ProductItemRequest request) {
        return ApiResponse.<ProductItemResponse>builder()
                .result(productItemService.addProduct(request))
                .build();
    }

    @RequestMapping(value = "/{productItemId}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteProductItem(@PathVariable String productItemId) {
        productItemService.deleteProductItem(productItemId);
        return ApiResponse.<String>builder().result("Product has been deleted").build();
    }
}
