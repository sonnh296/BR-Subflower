package com.hls.sunflower.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hls.sunflower.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, String>, JpaSpecificationExecutor<CartItem> {
    Optional<CartItem> findByCart_IdAndProduct_Id(String cartId, String productId);

    @Query("SELECT ci FROM CartItem ci " + "LEFT JOIN FETCH ci.product p "
            + "LEFT JOIN FETCH p.productImages "
            + "WHERE ci.cart.user.id = :userId")
    Page<CartItem> findCartItemsWithProductAndImagesByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT ci FROM CartItem ci " + "LEFT JOIN FETCH ci.product p "
            + "LEFT JOIN FETCH p.productImages "
            + "WHERE ci.id = :cartItemId")
    Optional<CartItem> findCartItemWithProductAndImagesById(@Param("cartItemId") String cartItemId);
}
