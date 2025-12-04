package com.hls.sunflower.service.serviceImpl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dao.ProductRepository;
import com.hls.sunflower.dao.ProductVariantRepository;
import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductListResponse;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductImage;
import com.hls.sunflower.entity.ProductVariant;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.mapper.ProductMapper;
import com.hls.sunflower.service.AzureBlobStorageService;
import com.hls.sunflower.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMapper productMapper;
    private final AzureBlobStorageService azureBlobStorageService;

    @Override
    public Page<ProductListResponse> getProducts(String field, Integer pageNumber, Integer pageSize, String sort) {
        Specification<Product> specs = Specification.where(null);

        Sort sortable = sort.equalsIgnoreCase("ASC")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        return productRepository.findAll(specs, pageable).map(productMapper::toProductListResponse);
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

        // Handle product images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            request.getImageUrls().forEach(url -> {
                ProductImage image =
                        ProductImage.builder().imageUrl(url).product(product).build();
                product.getProductImages().add(image);
            });
        }

        // Handle product variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            request.getVariants().forEach(variantRequest -> {
                ProductVariant variant = ProductVariant.builder()
                        .size(variantRequest.getSize())
                        .price(variantRequest.getPrice())
                        .stock(variantRequest.getStock())
                        .product(product)
                        .build();
                product.getVariants().add(variant);
            });
        }

        // Log mapped availability dates for debugging
        log.debug(
                "Mapped availableFrom={} availableTo={} for product={}",
                product.getAvailableFrom(),
                product.getAvailableTo(),
                product.getName());

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        productMapper.updateProductFromRequest(request, product);

        // Log mapped availability dates for debugging
        log.debug(
                "Updated mapping availableFrom={} availableTo={} for productId={}",
                product.getAvailableFrom(),
                product.getAvailableTo(),
                productId);

        // FIXED: Only update images if imageUrls are explicitly provided and not empty
        // This prevents accidental deletion of images when updating other fields
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            product.getProductImages().clear();
            request.getImageUrls().forEach(url -> {
                ProductImage image =
                        ProductImage.builder().imageUrl(url).product(product).build();
                product.getProductImages().add(image);
            });
            log.debug(
                    "Updated {} images for productId={}", request.getImageUrls().size(), productId);
        } else {
            log.debug(
                    "Keeping existing {} images for productId={}",
                    product.getProductImages().size(),
                    productId);
        }

        // FIXED: Only update variants if they are explicitly provided and not empty
        // This prevents accidental deletion of variants when updating other fields
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            product.getVariants().clear();
            request.getVariants().forEach(variantRequest -> {
                ProductVariant variant = ProductVariant.builder()
                        .size(variantRequest.getSize())
                        .price(variantRequest.getPrice())
                        .stock(variantRequest.getStock())
                        .product(product)
                        .build();
                product.getVariants().add(variant);
            });
            log.debug(
                    "Updated {} variants for productId={}",
                    request.getVariants().size(),
                    productId);
        } else {
            log.debug(
                    "Keeping existing {} variants for productId={}",
                    product.getVariants().size(),
                    productId);
        }

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(String productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public ProductResponse uploadProductImages(String productId, List<MultipartFile> images) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        // Upload images to Azure Blob Storage and get URLs
        List<String> imageUrls = azureBlobStorageService.uploadProductImages(images, productId);

        // Create ProductImage entities and add them to the product
        imageUrls.forEach(url -> {
            ProductImage productImage =
                    ProductImage.builder().imageUrl(url).product(product).build();
            product.getProductImages().add(productImage);
        });

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    public ProductResponse addProductImage(String productId, MultipartFile image) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        // Upload single image to Azure Blob Storage
        String imageUrl = azureBlobStorageService.uploadProductImage(image, productId);

        // Create ProductImage entity and add it to the product
        ProductImage productImage =
                ProductImage.builder().imageUrl(imageUrl).product(product).build();
        product.getProductImages().add(productImage);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    public ProductResponse removeProductImage(String productId, String imageId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        // Find the image to delete from Azure
        ProductImage imageToRemove = product.getProductImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElse(null);

        if (imageToRemove != null) {
            // Delete from Azure Blob Storage
            try {
                azureBlobStorageService.deleteFile(imageToRemove.getImageUrl());
            } catch (Exception e) {
                // Log error but continue to remove from database
            }

            // Remove from product
            product.getProductImages().remove(imageToRemove);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }
}
