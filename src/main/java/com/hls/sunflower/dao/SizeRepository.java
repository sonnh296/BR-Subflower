package com.hls.sunflower.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hls.sunflower.entity.Size;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
    Optional<Size> findByName(String name);

    List<Size> findByCategoryId(String categoryId);
}
