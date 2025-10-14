package com.hls.sunflower.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductListResponse;
import com.hls.sunflower.dto.response.ProductResponse;

public interface ProductService {
    Page<ProductListResponse> getProducts(String field, Integer pageNumber, Integer pageSize, String sort);

    ProductResponse getById(String id);

    ProductResponse addProduct(ProductRequest request);

    ProductResponse updateProduct(String productId, ProductRequest request);

    void deleteProduct(String productId);

    ProductResponse uploadProductImages(String productId, List<MultipartFile> images);

    ProductResponse addProductImage(String productId, MultipartFile image);

    ProductResponse removeProductImage(String productId, String imageId);
}
