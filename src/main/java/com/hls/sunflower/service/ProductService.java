package com.hls.sunflower.service;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getProducts(String field, Integer pageNumber, Integer pageSize, String sort);

    ProductResponse getById(String id);

    ProductResponse addProduct(ProductRequest request);

    ProductResponse updateProduct(String productId, ProductRequest request);

    void deleteProduct(String productId);
}
