package com.hls.sunflower.dao.specification;

import org.springframework.data.jpa.domain.Specification;

import com.hls.sunflower.entity.ProductItem;

public class ProductItemSpecification {
    public static Specification<ProductItem> equalProductId(String productId) {
        return (root, query, cb) -> cb.equal(root.get("product").get("id"), productId);
    }
}
