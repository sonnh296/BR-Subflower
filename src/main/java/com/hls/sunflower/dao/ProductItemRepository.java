package com.hls.sunflower.dao;

import com.hls.sunflower.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductItemRepository extends JpaRepository<ProductItem, String>, JpaSpecificationExecutor<ProductItem> {
}