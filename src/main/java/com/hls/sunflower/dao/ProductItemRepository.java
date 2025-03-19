package com.hls.sunflower.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hls.sunflower.entity.ProductItem;

public interface ProductItemRepository
        extends JpaRepository<ProductItem, String>, JpaSpecificationExecutor<ProductItem> {}
