package com.hls.sunflower.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hls.sunflower.entity.Banner;

public interface BannerRepository extends JpaRepository<Banner, String> {
    Optional<Banner> findByActiveTrue();
}
