package com.hls.sunflower.service;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductItemResponse;
import com.hls.sunflower.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

public interface ProductItemService {
    Page<ProductItemResponse> getProductItemList(String field, Integer pageNumber, Integer pageSize, String sort, String productId);

    ProductItemResponse getById(String id);

    void deleteProductItem(String productId);
}
