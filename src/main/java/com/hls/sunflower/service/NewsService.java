package com.hls.sunflower.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dto.request.NewsRequest;
import com.hls.sunflower.dto.response.NewsResponse;

public interface NewsService {
    NewsResponse createNews(NewsRequest request);

    NewsResponse updateNews(String newsId, NewsRequest request);

    void deleteNews(String newsId);

    NewsResponse getNewsById(String newsId);

    Page<NewsResponse> getPublishedNews(String field, Integer pageNumber, Integer pageSize, String sort);
    // New: upload image for a news item
    NewsResponse uploadNewsImage(String newsId, MultipartFile image);
}
