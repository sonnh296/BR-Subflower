package com.hls.sunflower.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductListResponse;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductImage;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "productItem", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    Product toProduct(ProductRequest request);

    @Mapping(source = "productImages", target = "imageUrls", qualifiedByName = "mapImagesToUrls")
    ProductResponse toProductResponse(Product product);

    @Mapping(source = "productImages", target = "thumbnailUrl", qualifiedByName = "mapFirstImageToUrl")
    ProductListResponse toProductListResponse(Product product);

    @Mapping(target = "productItem", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);

    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(List<ProductImage> productImages) {
        if (productImages == null) {
            return new ArrayList<>();
        }
        return productImages.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
    }

    @Named("mapFirstImageToUrl")
    default String mapFirstImageToUrl(List<ProductImage> productImages) {
        if (productImages == null || productImages.isEmpty()) {
            return null;
        }
        return productImages.get(0).getImageUrl();
    }
}
