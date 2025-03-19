package com.hls.sunflower.service.serviceImpl;

import com.hls.sunflower.dao.ProductRepository;
import com.hls.sunflower.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hls.sunflower.dao.ProductItemRepository;
import com.hls.sunflower.dao.specification.ProductItemSpecification;
import com.hls.sunflower.dto.request.ProductItemRequest;
import com.hls.sunflower.dto.response.ProductItemResponse;
import com.hls.sunflower.entity.ProductItem;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.mapper.ProductItemMapper;
import com.hls.sunflower.service.ProductItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductItemServiceImpl implements ProductItemService {
    private final ProductItemRepository productItemRepository;
    private final ProductRepository productRepository;
    private final ProductItemMapper productItemMapper;

    @Override
    public Page<ProductItemResponse> getProductItemList(
            String field, Integer pageNumber, Integer pageSize, String sort, String productId) {
        Specification<ProductItem> specs = Specification.where(null);

        if (productId != null && !productId.trim().isEmpty()) {
            specs = specs.and(ProductItemSpecification.equalProductId(productId));
        }

        Sort sortable = sort.equalsIgnoreCase("ASC")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        return productItemRepository.findAll(specs, pageable).map(productItemMapper::toProductItemResponse);
    }

    @Override
    public ProductItemResponse getById(String id) {
        ProductItem productItem = productItemRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_ITEM_NOT_EXISTED));
        return productItemMapper.toProductItemResponse(productItem);
    }

    @Override
    public ProductItemResponse updateProductItem(String productItemId, ProductItemRequest request) {
        ProductItem productItem = productItemRepository
                .findById(productItemId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_ITEM_NOT_EXISTED));
        productItemMapper.updateProductItemFromRequest(request, productItem);
        return productItemMapper.toProductItemResponse(productItemRepository.save(productItem));
    }

    @Override
    public ProductItemResponse addProduct(ProductItemRequest request) {
        Product product = productRepository.getReferenceById(request.getProductId());
        ProductItem productItem = ProductItem.builder()
                .product(product)
                .price(request.getPrice())
                .category(request.getCategory())
                .size(request.getSize())
                .color(request.getColor())
                .stockQuantity(request.getStockQuantity())
                .isActive(request.isActive())
                .url(request.getUrl())
                .build();
        return productItemMapper.toProductItemResponse(productItemRepository.save(productItem));
    }

    @Override
    public void deleteProductItem(String productId) {
        ProductItem productItem = productItemRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_ITEM_NOT_EXISTED));
        productItemRepository.delete(productItem);
    }
}
