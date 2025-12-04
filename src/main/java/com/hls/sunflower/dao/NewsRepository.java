package com.hls.sunflower.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hls.sunflower.entity.News;

public interface NewsRepository extends JpaRepository<News, String> {
    Page<News> findAllByPublishedTrue(Pageable pageable);
}
