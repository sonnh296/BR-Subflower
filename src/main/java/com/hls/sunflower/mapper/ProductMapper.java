package com.hls.sunflower.mapper;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "productItem", ignore = true)
    Product toProduct(ProductRequest request);

    ProductResponse toProductResponse(Product product);

    @Mapping(target = "productItem", ignore = true)
    void updateProductFromRequest(ProductRequest request,@MappingTarget Product product);
}
