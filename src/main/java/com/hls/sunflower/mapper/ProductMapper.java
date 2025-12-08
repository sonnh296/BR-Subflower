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
import com.hls.sunflower.dto.response.ProductOptionResponse;
import com.hls.sunflower.dto.response.ProductOptionWithVariantsResponse;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.dto.response.ProductVariantResponse;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductImage;
import com.hls.sunflower.entity.ProductOption;
import com.hls.sunflower.entity.ProductVariant;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "productOptions", ignore = true)
    @Mapping(source = "availableFrom", target = "availableFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(source = "availableTo", target = "availableTo", qualifiedByName = "toLocalDateTime")
    Product toProduct(ProductRequest request);

    @Mapping(source = "productImages", target = "imageUrls", qualifiedByName = "mapImagesToUrls")
    @Mapping(source = "productImages", target = "images", qualifiedByName = "mapImagesToImageResponses")
    @Mapping(source = "variants", target = "variants", qualifiedByName = "mapVariantsToResponse")
    @Mapping(
            target = "categoryId",
            expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)")
    @Mapping(
            target = "categoryName",
            expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
    @Mapping(source = "availableFrom", target = "availableFrom", qualifiedByName = "toIsoString")
    @Mapping(source = "availableTo", target = "availableTo", qualifiedByName = "toIsoString")
    @Mapping(source = "productOptions", target = "productOptions", qualifiedByName = "mapOptionsToResponse")
    @Mapping(target = "optionsWithVariants", expression = "java(mapOptionsWithVariants(product))")
    ProductResponse toProductResponse(Product product);

    @Mapping(source = "productImages", target = "thumbnailUrl", qualifiedByName = "mapFirstImageToUrl")
    @Mapping(source = "productImages", target = "imageUrls", qualifiedByName = "mapImagesToUrls")
    @Mapping(source = "variants", target = "variants", qualifiedByName = "mapVariantsToResponse")
    @Mapping(
            target = "categoryId",
            expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)")
    @Mapping(
            target = "categoryName",
            expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
    @Mapping(source = "productOptions", target = "productOptions", qualifiedByName = "mapOptionsToResponse")
    @Mapping(target = "optionsWithVariants", expression = "java(mapOptionsWithVariants(product))")
    ProductListResponse toProductListResponse(Product product);

    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "productOptions", ignore = true)
    @Mapping(source = "availableFrom", target = "availableFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(source = "availableTo", target = "availableTo", qualifiedByName = "toLocalDateTime")
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);

    // Variant mapping
    // Provide a default mapping to include sizeId and size (name) from the Size entity
    default ProductVariantResponse toProductVariantResponse(ProductVariant variant) {
        if (variant == null) return null;
        String sizeId = null;
        String sizeName = null;
        String optionId = null;
        String optionName = null;
        if (variant.getSize() != null) {
            sizeId = variant.getSize().getId();
            sizeName = variant.getSize().getName();
        }
        if (variant.getProductOption() != null) {
            optionId = variant.getProductOption().getId();
            optionName = variant.getProductOption().getName();
        }
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .sizeId(sizeId)
                .size(sizeName)
                .price(variant.getPrice())
                .stock(variant.getStock())
                .productOptionId(optionId)
                .productOptionName(optionName)
                .build();
    }

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

    @Named("mapOptionsToResponse")
    default List<ProductOptionResponse> mapOptionsToResponse(List<ProductOption> options) {
        if (options == null) return new ArrayList<>();
        return options.stream()
                .map(opt -> {
                    ProductOptionResponse resp = new ProductOptionResponse();
                    resp.setId(opt.getId());
                    resp.setName(opt.getName());
                    if (opt.getSizes() != null) {
                        resp.setSizeIds(
                                opt.getSizes().stream().map(s -> s.getId()).collect(Collectors.toList()));
                        resp.setSizeNames(
                                opt.getSizes().stream().map(s -> s.getName()).collect(Collectors.toList()));
                    }
                    return resp;
                })
                .collect(Collectors.toList());
    }

    // New: group variants per option for frontend convenience
    default List<ProductOptionWithVariantsResponse> mapOptionsWithVariants(Product product) {
        List<ProductOptionWithVariantsResponse> result = new ArrayList<>();
        if (product == null) return result;

        // group by option
        List<ProductOption> options = product.getProductOptions();
        List<ProductVariant> variants = product.getVariants();

        // For each option, collect variants referencing it
        if (options != null) {
            for (ProductOption opt : options) {
                ProductOptionWithVariantsResponse grp = new ProductOptionWithVariantsResponse();
                grp.setOptionId(opt.getId());
                grp.setOptionName(opt.getName());
                List<ProductVariantResponse> vs = new ArrayList<>();
                if (variants != null) {
                    for (ProductVariant v : variants) {
                        if (v.getProductOption() != null
                                && opt.getId().equals(v.getProductOption().getId())) {
                            vs.add(toProductVariantResponse(v));
                        }
                    }
                }
                grp.setVariants(vs);
                result.add(grp);
            }
        }

        // Add a default group for variants that don't belong to any option
        List<ProductVariantResponse> unassigned = new ArrayList<>();
        if (variants != null) {
            for (ProductVariant v : variants) {
                if (v.getProductOption() == null) {
                    unassigned.add(toProductVariantResponse(v));
                }
            }
        }
        if (!unassigned.isEmpty()) {
            ProductOptionWithVariantsResponse defaultGrp = new ProductOptionWithVariantsResponse();
            defaultGrp.setOptionId(null);
            defaultGrp.setOptionName("Default");
            defaultGrp.setVariants(unassigned);
            result.add(defaultGrp);
        }

        return result;
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
