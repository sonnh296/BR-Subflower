package com.hls.sunflower.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.hls.sunflower.dto.request.ProductItemRequest;
import com.hls.sunflower.dto.response.ProductItemResponse;
import com.hls.sunflower.entity.ProductItem;

@Mapper(componentModel = "spring")
public interface ProductItemMapper {
    ProductItem toProductItem(ProductItemRequest request);

    ProductItemResponse toProductItemResponse(ProductItem productItem);

    void updateProductItemFromRequest(ProductItemRequest request, @MappingTarget ProductItem productItem);
}
