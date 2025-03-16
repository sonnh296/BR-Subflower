package com.hls.sunflower.dao.specification;

import com.hls.sunflower.entity.ProductItem;
import org.springframework.data.jpa.domain.Specification;

public class ProductItemSpecification {
    public static Specification<ProductItem> equalProductId(String productId) {
        return (root, query, cb) -> cb.equal(root.get("product").get("id"), productId);
    }
}
