package com.hls.sunflower.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hls.sunflower.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);
}
