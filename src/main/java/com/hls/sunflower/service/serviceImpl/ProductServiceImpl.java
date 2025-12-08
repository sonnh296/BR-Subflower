package com.hls.sunflower.service.serviceImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dao.CategoryRepository;
import com.hls.sunflower.dao.ProductOptionRepository;
import com.hls.sunflower.dao.ProductRepository;
import com.hls.sunflower.dao.ProductVariantRepository;
import com.hls.sunflower.dao.SizeRepository;
import com.hls.sunflower.dto.request.ProductOptionRequest;
import com.hls.sunflower.dto.request.ProductRequest;
import com.hls.sunflower.dto.response.ProductListResponse;
import com.hls.sunflower.dto.response.ProductResponse;
import com.hls.sunflower.entity.Category;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductImage;
import com.hls.sunflower.entity.ProductOption;
import com.hls.sunflower.entity.ProductVariant;
import com.hls.sunflower.entity.Size;
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
    private final SizeRepository sizeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOptionRepository productOptionRepository;
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

        // Handle product options first so variants can reference them
        if (request.getProductOptions() != null && !request.getProductOptions().isEmpty()) {
            for (ProductOptionRequest optReq : request.getProductOptions()) {
                ProductOption opt = ProductOption.builder()
                        .id(
                                optReq.getId() != null
                                        ? optReq.getId()
                                        : UUID.randomUUID().toString())
                        .name(optReq.getName())
                        .product(product)
                        .build();

                // Resolve sizes for this option
                if (optReq.getSizeIds() != null) {
                    for (String sid : optReq.getSizeIds()) {
                        Size s = sizeRepository
                                .findById(sid)
                                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                        opt.getSizes().add(s);
                    }
                }
                if (optReq.getSizes() != null) {
                    for (String sname : optReq.getSizes()) {
                        if (sname == null || sname.isBlank()) continue;
                        String name = sname.trim();
                        Size s = sizeRepository.findByName(name).orElse(null);
                        if (s == null) {
                            s = Size.builder()
                                    .id(UUID.randomUUID().toString())
                                    .name(name)
                                    .build();
                            sizeRepository.save(s);
                        }
                        opt.getSizes().add(s);
                    }
                }
                product.getProductOptions().add(opt);
            }
        }

        // Handle product variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            request.getVariants().forEach(variantRequest -> {
                Size sizeEntity = null;
                // Prefer explicit sizeId
                if (variantRequest.getSizeId() != null) {
                    sizeEntity = sizeRepository
                            .findById(variantRequest.getSizeId())
                            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                } else if (variantRequest.getSize() != null
                        && !variantRequest.getSize().isBlank()) {
                    // Backward-compat: try to find by name or create a new size
                    String sizeName = variantRequest.getSize().trim();
                    sizeEntity = sizeRepository.findByName(sizeName).orElse(null);
                    if (sizeEntity == null) {
                        sizeEntity = Size.builder()
                                .id(UUID.randomUUID().toString())
                                .name(sizeName)
                                .build();
                        sizeRepository.save(sizeEntity);
                    }
                }

                ProductVariant variant = ProductVariant.builder()
                        .size(sizeEntity)
                        .price(variantRequest.getPrice())
                        .stock(variantRequest.getStock())
                        .product(product)
                        .build();

                // Assign product option if provided
                if (variantRequest.getProductOptionId() != null || variantRequest.getProductOptionName() != null) {
                    String optId = variantRequest.getProductOptionId();
                    String optName = variantRequest.getProductOptionName();
                    ProductOption found = null;
                    if (optId != null) {
                        found = product.getProductOptions().stream()
                                .filter(o -> o.getId().equals(optId))
                                .findFirst()
                                .orElse(null);
                        if (found == null) {
                            // try repository as fallback
                            found = productOptionRepository.findById(optId).orElse(null);
                        }
                    }
                    if (found == null && optName != null) {
                        found = product.getProductOptions().stream()
                                .filter(o -> optName.equals(o.getName()))
                                .findFirst()
                                .orElse(null);
                        if (found == null) {
                            found = productOptionRepository.findByName(optName).orElse(null);
                        }
                    }
                    if (found != null) {
                        variant.setProductOption(found);
                    }
                }

                product.getVariants().add(variant);
            });
        }

        // Set category if provided
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            String catInput = request.getCategoryId().trim();
            // Try treat as id first
            Category category = categoryRepository.findById(catInput).orElse(null);
            if (category == null) {
                // Try find by name
                category = categoryRepository.findByName(catInput).orElse(null);
            }
            if (category == null) {
                // Create new category with this name
                category = Category.builder()
                        .id(UUID.randomUUID().toString())
                        .name(catInput)
                        .build();
                categoryRepository.save(category);
            }
            product.setCategory(category);
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

        // Debug: log incoming variant and productOptions payload for traceability
        if (request.getVariants() != null) {
            request.getVariants().forEach(vr -> {
                log.debug(
                        "Incoming variantRequest: sizeId={} size={} price={} stock={} productOptionId={} productOptionName={}",
                        vr.getSizeId(),
                        vr.getSize(),
                        vr.getPrice(),
                        vr.getStock(),
                        vr.getProductOptionId(),
                        vr.getProductOptionName());
            });
        }
        if (request.getProductOptions() != null) {
            request.getProductOptions().forEach(po -> {
                log.debug(
                        "Incoming productOptionRequest: id={} name={} sizes={} sizeIds={}",
                        po.getId(),
                        po.getName(),
                        po.getSizes(),
                        po.getSizeIds());
            });
        }

        // Update category if provided in the request
        // Semantic: request.getCategoryId() == null => no change; empty string => clear category; non-empty =>
        // set/find/create
        if (request.getCategoryId() != null) {
            if (request.getCategoryId().isBlank()) {
                // Explicit clear request
                product.setCategory(null);
                log.debug("Cleared category for productId={}", productId);
            } else {
                String catInput = request.getCategoryId().trim();
                // Try treat as id first
                Category category = categoryRepository.findById(catInput).orElse(null);
                if (category == null) {
                    // Try find by name
                    category = categoryRepository.findByName(catInput).orElse(null);
                }
                if (category == null) {
                    // Create new category with this name
                    category = Category.builder()
                            .id(UUID.randomUUID().toString())
                            .name(catInput)
                            .build();
                    categoryRepository.save(category);
                }
                product.setCategory(category);
            }
        }

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

        // Update product options if provided
        if (request.getProductOptions() != null && !request.getProductOptions().isEmpty()) {
            List<ProductOption> existingOptions = product.getProductOptions();
            // Keep track of which existing options have been matched to avoid duplicate mapping
            java.util.List<ProductOption> unmatched = new java.util.ArrayList<>(existingOptions);

            for (int idx = 0; idx < request.getProductOptions().size(); idx++) {
                ProductOptionRequest optReq = request.getProductOptions().get(idx);
                ProductOption existing = null;

                // Try to resolve by id when provided
                if (optReq.getId() != null) {
                    existing = existingOptions.stream()
                            .filter(o -> optReq.getId().equals(o.getId()))
                            .findFirst()
                            .orElse(null);
                    if (existing == null) {
                        existing =
                                productOptionRepository.findById(optReq.getId()).orElse(null);
                    }
                }

                // If not found by id, try resolving by name (trimmed, case-insensitive)
                if (existing == null
                        && optReq.getName() != null
                        && !optReq.getName().isBlank()) {
                    String optName = optReq.getName().trim();
                    existing = unmatched.stream()
                            .filter(o -> o.getName() != null
                                    && optName.equalsIgnoreCase(o.getName().trim()))
                            .findFirst()
                            .orElse(null);
                    if (existing == null) {
                        existing = productOptionRepository.findByName(optName).orElse(null);
                        // If found in repository, ensure it's still part of this product
                        if (existing != null && !existingOptions.contains(existing)) {
                            existing = null;
                        }
                    }
                }

                // If still not found, attempt size-overlap matching as a heuristic
                if (existing == null) {
                    // collect requested size ids/names
                    java.util.Set<String> reqSizeIds = new java.util.HashSet<>();
                    java.util.Set<String> reqSizeNames = new java.util.HashSet<>();
                    if (optReq.getSizeIds() != null) reqSizeIds.addAll(optReq.getSizeIds());
                    if (optReq.getSizes() != null) {
                        for (String sname : optReq.getSizes()) {
                            if (sname != null && !sname.isBlank())
                                reqSizeNames.add(sname.trim().toLowerCase());
                        }
                    }

                    int bestScore = 0;
                    ProductOption bestMatch = null;
                    for (ProductOption cand : new java.util.ArrayList<>(unmatched)) {
                        if (cand == null) continue;
                        int score = 0;
                        for (Size s : cand.getSizes()) {
                            if (s == null) continue;
                            if (s.getId() != null && reqSizeIds.contains(s.getId())) score++;
                            if (s.getName() != null
                                    && reqSizeNames.contains(s.getName().trim().toLowerCase())) score++;
                        }
                        if (score > bestScore) {
                            bestScore = score;
                            bestMatch = cand;
                        }
                    }
                    if (bestScore > 0 && bestMatch != null) {
                        existing = bestMatch;
                        log.debug(
                                "Matched ProductOption by size-overlap: optReq.name={} -> existing.id={} (score={}) for productId={}",
                                optReq.getName(),
                                existing.getId(),
                                bestScore,
                                productId);
                    }
                }

                // If still not found, attempt positional matching (same index) as a best-effort to detect renames
                if (existing == null && optReq.getId() == null && idx < existingOptions.size()) {
                    ProductOption posCandidate = existingOptions.get(idx);
                    if (unmatched.contains(posCandidate)) {
                        existing = posCandidate;
                        log.debug(
                                "Positional match: using existing ProductOption id={} at index {} for productId={}",
                                existing.getId(),
                                idx,
                                productId);
                    }
                }

                // If we resolved an existing option, mark it matched (remove from unmatched)
                if (existing != null) {
                    unmatched.remove(existing);
                }

                if (existing == null) {
                    // create new option and attach to product
                    ProductOption opt = ProductOption.builder()
                            .id(
                                    optReq.getId() != null
                                            ? optReq.getId()
                                            : UUID.randomUUID().toString())
                            .name(optReq.getName())
                            .product(product)
                            .build();
                    if (optReq.getSizeIds() != null) {
                        for (String sid : optReq.getSizeIds()) {
                            Size s = sizeRepository
                                    .findById(sid)
                                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                            opt.getSizes().add(s);
                        }
                    }
                    if (optReq.getSizes() != null) {
                        for (String sname : optReq.getSizes()) {
                            if (sname == null || sname.isBlank()) continue;
                            String name = sname.trim();
                            Size s = sizeRepository.findByName(name).orElse(null);
                            if (s == null) {
                                s = Size.builder()
                                        .id(UUID.randomUUID().toString())
                                        .name(name)
                                        .build();
                                sizeRepository.save(s);
                            }
                            opt.getSizes().add(s);
                        }
                    }
                    product.getProductOptions().add(opt);
                } else {
                    // update existing option's name and sizes
                    if (optReq.getName() != null) existing.setName(optReq.getName());
                    if (optReq.getSizeIds() != null && !optReq.getSizeIds().isEmpty()) {
                        existing.getSizes().clear();
                        for (String sid : optReq.getSizeIds()) {
                            Size s = sizeRepository
                                    .findById(sid)
                                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                            existing.getSizes().add(s);
                        }
                    }
                    if (optReq.getSizes() != null && !optReq.getSizes().isEmpty()) {
                        for (String sname : optReq.getSizes()) {
                            if (sname == null || sname.isBlank()) continue;
                            String name = sname.trim();
                            Size s = sizeRepository.findByName(name).orElse(null);
                            if (s == null) {
                                s = Size.builder()
                                        .id(UUID.randomUUID().toString())
                                        .name(name)
                                        .build();
                                sizeRepository.save(s);
                            }
                            if (!existing.getSizes().contains(s))
                                existing.getSizes().add(s);
                        }
                    }
                }
            }

            // Cleanup: remove any unmatched existing options that are not referenced by variants
            if (!unmatched.isEmpty()) {
                java.util.List<ProductOption> toRemove = new java.util.ArrayList<>();
                for (ProductOption u : new java.util.ArrayList<>(unmatched)) {
                    if (u == null) continue;
                    boolean usedByVariant = product.getVariants().stream()
                            .anyMatch(v -> v.getProductOption() != null
                                    && u.getId() != null
                                    && u.getId().equals(v.getProductOption().getId()));
                    if (!usedByVariant) {
                        toRemove.add(u);
                    }
                }
                for (ProductOption rem : toRemove) {
                    try {
                        product.getProductOptions().remove(rem);
                        productOptionRepository.deleteById(rem.getId());
                        log.debug(
                                "Removed unused ProductOption id={} name={} from productId={}",
                                rem.getId(),
                                rem.getName(),
                                productId);
                    } catch (Exception ex) {
                        log.warn(
                                "Failed to delete ProductOption id={} for productId={} : {}",
                                rem.getId(),
                                productId,
                                ex.getMessage());
                    }
                }
            }

            // Deduplicate product options: group by normalized name + sizeIds (sorted) and merge duplicates
            if (!product.getProductOptions().isEmpty()) {
                java.util.Map<String, java.util.List<ProductOption>> groups = new java.util.HashMap<>();
                for (ProductOption po : new java.util.ArrayList<>(product.getProductOptions())) {
                    if (po == null) continue;
                    String nameKey =
                            po.getName() == null ? "" : po.getName().trim().toLowerCase();
                    java.util.List<String> sids = new java.util.ArrayList<>();
                    for (Size s : po.getSizes()) {
                        if (s == null) continue;
                        String nm =
                                s.getName() == null ? "" : s.getName().trim().toLowerCase();
                        sids.add(nm);
                    }
                    java.util.Collections.sort(sids);
                    String key = nameKey + "|" + String.join(",", sids);
                    groups.computeIfAbsent(key, k -> new java.util.ArrayList<>())
                            .add(po);
                }

                for (java.util.Map.Entry<String, java.util.List<ProductOption>> e : groups.entrySet()) {
                    java.util.List<ProductOption> list = e.getValue();
                    if (list.size() <= 1) continue;
                    // choose canonical: prefer one that is referenced by any variant, otherwise first
                    ProductOption canonical = null;
                    for (ProductOption cand : list) {
                        boolean used = product.getVariants().stream()
                                .anyMatch(v -> v.getProductOption() != null
                                        && cand.getId() != null
                                        && cand.getId()
                                                .equals(v.getProductOption().getId()));
                        if (used) {
                            canonical = cand;
                            break;
                        }
                    }
                    if (canonical == null) canonical = list.get(0);

                    for (ProductOption dup : list) {
                        if (dup == null || dup.getId() == null) continue;
                        if (dup.getId().equals(canonical.getId())) continue;
                        // reassign variants pointing to dup -> canonical
                        for (ProductVariant v : product.getVariants()) {
                            if (v.getProductOption() != null
                                    && dup.getId().equals(v.getProductOption().getId())) {
                                v.setProductOption(canonical);
                            }
                        }
                        // remove dup from product options and delete repository entry
                        try {
                            product.getProductOptions().remove(dup);
                            productOptionRepository.deleteById(dup.getId());
                            log.debug(
                                    "Merged and removed duplicate ProductOption id={} into id={} for productId={}",
                                    dup.getId(),
                                    canonical.getId(),
                                    productId);
                        } catch (Exception ex) {
                            log.warn(
                                    "Failed to delete duplicate ProductOption id={} for productId={} : {}",
                                    dup.getId(),
                                    productId,
                                    ex.getMessage());
                        }
                    }
                }
            }
        }

        // FIXED: Only update variants if they are explicitly provided and not empty
        // This prevents accidental deletion of variants when updating other fields
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // Instead of clearing all variants (which causes FK constraint errors),
            // we update existing variants and add new ones
            List<ProductVariant> existingVariants = product.getVariants();

            // Create a map of existing variants by index for easier access
            // Update existing variants or create new ones
            for (int i = 0; i < request.getVariants().size(); i++) {
                var variantRequest = request.getVariants().get(i);

                if (i < existingVariants.size()) {
                    // Update existing variant in place
                    ProductVariant existingVariant = existingVariants.get(i);
                    Size sizeEntity = null;
                    if (variantRequest.getSizeId() != null) {
                        sizeEntity = sizeRepository
                                .findById(variantRequest.getSizeId())
                                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                    } else if (variantRequest.getSize() != null
                            && !variantRequest.getSize().isBlank()) {
                        String sizeName = variantRequest.getSize().trim();
                        sizeEntity = sizeRepository.findByName(sizeName).orElse(null);
                        if (sizeEntity == null) {
                            sizeEntity = Size.builder()
                                    .id(UUID.randomUUID().toString())
                                    .name(sizeName)
                                    .build();
                            sizeRepository.save(sizeEntity);
                        }
                    }
                    existingVariant.setSize(sizeEntity);
                    existingVariant.setPrice(variantRequest.getPrice());
                    existingVariant.setStock(variantRequest.getStock());

                    // update product option link if provided
                    if (variantRequest.getProductOptionId() != null || variantRequest.getProductOptionName() != null) {
                        String optId = variantRequest.getProductOptionId();
                        String optName = variantRequest.getProductOptionName();
                        ProductOption found = null;
                        if (optId != null) {
                            found = product.getProductOptions().stream()
                                    .filter(o -> o.getId().equals(optId))
                                    .findFirst()
                                    .orElse(null);
                            if (found == null) {
                                found = productOptionRepository.findById(optId).orElse(null);
                            }
                        }
                        if (found == null && optName != null) {
                            found = product.getProductOptions().stream()
                                    .filter(o -> optName.equals(o.getName()))
                                    .findFirst()
                                    .orElse(null);
                            if (found == null) {
                                found = productOptionRepository
                                        .findByName(optName)
                                        .orElse(null);
                            }
                        }
                        // If still not found but a name was provided, create the option
                        if (found == null && optName != null) {
                            ProductOption newOpt = ProductOption.builder()
                                    .id(UUID.randomUUID().toString())
                                    .name(optName)
                                    .product(product)
                                    .build();
                            // Persist the new option and attach to product
                            // Also attach the sizeEntity (if known) so the option is useful
                            if (sizeEntity != null) {
                                newOpt.getSizes().add(sizeEntity);
                            }
                            productOptionRepository.save(newOpt);
                            product.getProductOptions().add(newOpt);
                            found = newOpt;
                            log.debug("Created new ProductOption '{}' for productId={}", optName, productId);
                        }
                        if (found != null) {
                            existingVariant.setProductOption(found);
                        }
                    }

                    log.debug("Updated existing variant {} for productId={}", existingVariant.getId(), productId);
                } else {
                    // Add new variant
                    Size sizeEntity = null;
                    if (variantRequest.getSizeId() != null) {
                        sizeEntity = sizeRepository
                                .findById(variantRequest.getSizeId())
                                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                    } else if (variantRequest.getSize() != null
                            && !variantRequest.getSize().isBlank()) {
                        String sizeName = variantRequest.getSize().trim();
                        sizeEntity = sizeRepository.findByName(sizeName).orElse(null);
                        if (sizeEntity == null) {
                            sizeEntity = Size.builder()
                                    .id(UUID.randomUUID().toString())
                                    .name(sizeName)
                                    .build();
                            sizeRepository.save(sizeEntity);
                        }
                    }
                    ProductVariant newVariant = ProductVariant.builder()
                            .size(sizeEntity)
                            .price(variantRequest.getPrice())
                            .stock(variantRequest.getStock())
                            .product(product)
                            .build();

                    // Assign product option if provided
                    if (variantRequest.getProductOptionId() != null || variantRequest.getProductOptionName() != null) {
                        String optId = variantRequest.getProductOptionId();
                        String optName = variantRequest.getProductOptionName();
                        ProductOption found = null;
                        if (optId != null) {
                            found = product.getProductOptions().stream()
                                    .filter(o -> o.getId().equals(optId))
                                    .findFirst()
                                    .orElse(null);
                            if (found == null) {
                                found = productOptionRepository.findById(optId).orElse(null);
                            }
                        }
                        if (found == null && optName != null) {
                            found = product.getProductOptions().stream()
                                    .filter(o -> optName.equals(o.getName()))
                                    .findFirst()
                                    .orElse(null);
                            if (found == null) {
                                found = productOptionRepository
                                        .findByName(optName)
                                        .orElse(null);
                            }
                        }
                        // If still not found but a name was provided, create the option
                        if (found == null && optName != null) {
                            ProductOption newOpt = ProductOption.builder()
                                    .id(UUID.randomUUID().toString())
                                    .name(optName)
                                    .product(product)
                                    .build();
                            // attach sizeEntity to option if available
                            if (sizeEntity != null) {
                                newOpt.getSizes().add(sizeEntity);
                            }
                            productOptionRepository.save(newOpt);
                            product.getProductOptions().add(newOpt);
                            found = newOpt;
                            log.debug("Created new ProductOption '{}' for productId={}", optName, productId);
                        }
                        if (found != null) {
                            newVariant.setProductOption(found);
                        }
                    }

                    existingVariants.add(newVariant);
                    log.debug("Added new variant for productId={}", productId);
                }
            }

            // Note: We don't remove excess variants to avoid FK constraint violations
            // If there are more existing variants than in the request, they will remain unchanged
            if (existingVariants.size() > request.getVariants().size()) {
                log.warn(
                        "Product {} has {} existing variants but only {} provided in update. Excess variants kept to avoid FK constraint violations.",
                        productId,
                        existingVariants.size(),
                        request.getVariants().size());
            }

            log.debug(
                    "Updated variants for productId={}: {} provided, {} existing",
                    productId,
                    request.getVariants().size(),
                    existingVariants.size());
        } else {
            log.debug(
                    "Keeping existing {} variants for productId={}",
                    product.getVariants().size(),
                    productId);
        }

        // Sanitize Size references before saving to avoid Hibernate trying to resolve missing Size proxies
        // If a Size referenced by an option or a variant no longer exists in DB, remove or clear that reference.
        // This prevents javax.persistence.EntityNotFoundException / JpaObjectRetrievalFailureException on save/merge.
        for (ProductOption opt : product.getProductOptions()) {
            if (opt == null) continue;
            java.util.List<Size> cleaned = new java.util.ArrayList<>();
            for (Size sRef : opt.getSizes()) {
                if (sRef == null) continue;
                String sid = sRef.getId();
                if (sid == null) continue;
                Size managed = sizeRepository.findById(sid).orElse(null);
                if (managed != null) {
                    cleaned.add(managed);
                } else {
                    log.warn(
                            "Removing missing Size id={} from ProductOption id={} of productId={}",
                            sid,
                            opt.getId(),
                            productId);
                }
            }
            opt.getSizes().clear();
            opt.getSizes().addAll(cleaned);
        }

        for (ProductVariant v : product.getVariants()) {
            if (v == null) continue;
            Size sRef = v.getSize();
            if (sRef == null) continue;
            String sid = sRef.getId();
            if (sid == null) continue;
            Size managed = sizeRepository.findById(sid).orElse(null);
            if (managed != null) {
                v.setSize(managed);
            } else {
                log.warn(
                        "Clearing missing Size id={} from ProductVariant id={} of productId={}",
                        sid,
                        v.getId(),
                        productId);
                v.setSize(null);
            }
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
