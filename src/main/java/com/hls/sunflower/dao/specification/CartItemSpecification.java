package com.hls.sunflower.dao.specification;

import org.springframework.data.jpa.domain.Specification;

import com.hls.sunflower.entity.CartItem;

public class CartItemSpecification {
    public static Specification<CartItem> equalUserId(String userId) {
        return (root, query, cb) -> cb.equal(root.get("cart").get("user").get("id"), userId);
    }
}
