// ...new file...
package com.hls.sunflower.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hls.sunflower.entity.ProductOption;

public interface ProductOptionRepository extends JpaRepository<ProductOption, String> {
    Optional<ProductOption> findByName(String name);
}
