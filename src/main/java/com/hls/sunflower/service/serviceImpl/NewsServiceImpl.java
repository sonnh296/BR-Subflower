package com.hls.sunflower.service.serviceImpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dao.NewsRepository;
import com.hls.sunflower.dto.request.NewsRequest;
import com.hls.sunflower.dto.response.NewsResponse;
import com.hls.sunflower.entity.News;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.service.AzureBlobStorageService;
import com.hls.sunflower.service.NewsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    @Override
    public NewsResponse createNews(NewsRequest request) {
        log.info("Creating news with title: {}, published: {}", request.getTitle(), request.getPublished());

        try {
            News news = News.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .imageUrl(request.getImageUrl())
                    .published(request.getPublished() != null ? request.getPublished() : false)
                    .build();

            log.info("Built news entity, saving to database...");
            News saved = newsRepository.save(news);
            log.info("News saved successfully with ID: {}", saved.getId());

            return toResponse(saved);
        } catch (Exception e) {
            log.error("Error creating news: ", e);
            throw e;
        }
    }

    @Override
    public NewsResponse updateNews(String newsId, NewsRequest request) {
        News news = newsRepository
                .findById(newsId)
                .orElseThrow(() -> new AppException(com.hls.sunflower.exception.ErrorCode.RESOURCE_NOT_FOUND));
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setImageUrl(request.getImageUrl());
        news.setPublished(request.getPublished() != null ? request.getPublished() : false);
        News saved = newsRepository.save(news);
        return toResponse(saved);
    }

    @Override
    public void deleteNews(String newsId) {
        if (!newsRepository.existsById(newsId)) {
            throw new AppException(com.hls.sunflower.exception.ErrorCode.RESOURCE_NOT_FOUND);
        }
        newsRepository.deleteById(newsId);
    }

    @Override
    public NewsResponse getNewsById(String newsId) {
        News news = newsRepository
                .findById(newsId)
                .orElseThrow(() -> new AppException(com.hls.sunflower.exception.ErrorCode.RESOURCE_NOT_FOUND));
        return toResponse(news);
    }

    @Override
    public Page<NewsResponse> getPublishedNews(String field, Integer pageNumber, Integer pageSize, String sort) {
        Sort sortable = sort.equalsIgnoreCase("ASC")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);
        return newsRepository.findAllByPublishedTrue(pageable).map(this::toResponse);
    }

    @Override
    public NewsResponse uploadNewsImage(String newsId, MultipartFile image) {
        News news = newsRepository
                .findById(newsId)
                .orElseThrow(() -> new AppException(com.hls.sunflower.exception.ErrorCode.RESOURCE_NOT_FOUND));
        String url = azureBlobStorageService.uploadFile(image, "news/" + newsId);
        news.setImageUrl(url);
        News saved = newsRepository.save(news);
        return toResponse(saved);
    }

    private NewsResponse toResponse(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .imageUrl(news.getImageUrl())
                .published(news.isPublished())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }
}
