package com.hls.sunflower.service;

import org.springframework.data.domain.Page;

import com.hls.sunflower.dto.request.ProductItemRequest;
import com.hls.sunflower.dto.response.ProductItemResponse;

public interface ProductItemService {
    Page<ProductItemResponse> getProductItemList(
            String field, Integer pageNumber, Integer pageSize, String sort, String productId);

    ProductItemResponse getById(String id);

    ProductItemResponse updateProductItem(String productItemId, ProductItemRequest request);

    ProductItemResponse addProduct(ProductItemRequest request);

    void deleteProductItem(String productId);
}
