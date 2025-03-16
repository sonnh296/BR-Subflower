package com.hls.sunflower.dao;

import com.hls.sunflower.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String>, JpaSpecificationExecutor<CartItem> {
    Optional<CartItem> findByCart_IdAndProductItem_Id(String cartId, String productItemId);
}