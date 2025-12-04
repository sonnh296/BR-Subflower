package com.hls.sunflower.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductImageResponse;
import com.hls.sunflower.dto.response.ProductListResponse;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.dto.response.ProductVariantResponse;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductImage;
import com.hls.sunflower.entity.ProductVariant;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(source = "availableFrom", target = "availableFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(source = "availableTo", target = "availableTo", qualifiedByName = "toLocalDateTime")
    Product toProduct(ProductRequest request);

    @Mapping(source = "productImages", target = "imageUrls", qualifiedByName = "mapImagesToUrls")
    @Mapping(source = "productImages", target = "images", qualifiedByName = "mapImagesToImageResponses")
    @Mapping(source = "variants", target = "variants", qualifiedByName = "mapVariantsToResponse")
    @Mapping(source = "availableFrom", target = "availableFrom", qualifiedByName = "toIsoString")
    @Mapping(source = "availableTo", target = "availableTo", qualifiedByName = "toIsoString")
    ProductResponse toProductResponse(Product product);

    @Mapping(source = "productImages", target = "thumbnailUrl", qualifiedByName = "mapFirstImageToUrl")
    ProductListResponse toProductListResponse(Product product);

    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(source = "availableFrom", target = "availableFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(source = "availableTo", target = "availableTo", qualifiedByName = "toLocalDateTime")
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);

    // Variant mapping
    ProductVariantResponse toProductVariantResponse(ProductVariant variant);

    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(List<ProductImage> productImages) {
        if (productImages == null) {
            return new ArrayList<>();
        }
        return productImages.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
    }

    @Named("mapImagesToImageResponses")
    default List<ProductImageResponse> mapImagesToImageResponses(List<ProductImage> productImages) {
        if (productImages == null) {
            return new ArrayList<>();
        }
        return productImages.stream()
                .map(img -> ProductImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("mapFirstImageToUrl")
    default String mapFirstImageToUrl(List<ProductImage> productImages) {
        if (productImages == null || productImages.isEmpty()) {
            return null;
        }
        return productImages.get(0).getImageUrl();
    }

    @Named("mapVariantsToResponse")
    default List<ProductVariantResponse> mapVariantsToResponse(List<ProductVariant> variants) {
        if (variants == null) {
            return new ArrayList<>();
        }
        return variants.stream().map(this::toProductVariantResponse).collect(Collectors.toList());
    }

    @Named("toLocalDateTime")
    default LocalDateTime toLocalDateTime(String value) {
        if (value == null) return null;
        // Try parsing ISO instant first (with timezone)
        try {
            Instant instant = Instant.parse(value);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (Exception e) {
            // ignore
        }
        // Try parsing LocalDateTime directly
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            // ignore
        }
        // Try parsing date-only
        try {
            LocalDate d = LocalDate.parse(value);
            return d.atStartOfDay();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @Named("toIsoString")
    default String toIsoString(LocalDateTime value) {
        if (value == null) return null;
        return value.atZone(ZoneId.systemDefault()).toInstant().toString();
    }
}
