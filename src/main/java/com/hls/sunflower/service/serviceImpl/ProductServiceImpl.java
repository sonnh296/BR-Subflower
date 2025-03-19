package com.hls.sunflower.service.serviceImpl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hls.sunflower.dao.ProductItemRepository;
import com.hls.sunflower.dao.ProductRepository;
import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductItem;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.mapper.ProductItemMapper;
import com.hls.sunflower.mapper.ProductMapper;
import com.hls.sunflower.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductMapper productMapper;
    private final ProductItemMapper productItemMapper;

    @Override
    public Page<ProductResponse> getProducts(String field, Integer pageNumber, Integer pageSize, String sort) {
        Specification<Product> specs = Specification.where(null);

        Sort sortable = sort.equalsIgnoreCase("ASC")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        return productRepository.findAll(specs, pageable).map(productMapper::toProductResponse);
    }

    @Override
    public ProductResponse getById(String id) {
        Product product =
                productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse addProduct(ProductRequest request) {
        Product product = productMapper.toProduct(request);

        Set<ProductItem> productItemSet = request.getProductItem().stream()
                .map(productItemRequest -> {
                    ProductItem productItem = productItemMapper.toProductItem(productItemRequest);
                    productItem.setProduct(product);
                    return productItem;
                })
                .collect(Collectors.toSet());
        product.setProductItem(productItemSet);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        productMapper.updateProductFromRequest(request, product);

        product.getProductItem().clear();

        request.getProductItem().stream().forEach(productItemRequest -> {
            ProductItem productItem = productItemMapper.toProductItem(productItemRequest);
            productItem.setProduct(product);
            product.getProductItem().add(productItem);
        });

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(String productId) {
        productRepository.deleteById(productId);
    }
}
