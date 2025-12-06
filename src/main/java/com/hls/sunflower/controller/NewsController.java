package com.hls.sunflower.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.dto.request.NewsRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.NewsResponse;
import com.hls.sunflower.service.NewsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {
    private final NewsService newsService;

    @GetMapping("")
    public ApiResponse<Page<NewsResponse>> getPublishedNews(
            @RequestParam(name = "field", required = false, defaultValue = "createdAt") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "DESC") String sort) {
        return ApiResponse.<Page<NewsResponse>>builder()
                .result(newsService.getPublishedNews(field, pageNumber, pageSize, sort))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsResponse> getNews(@PathVariable String id) {
        return ApiResponse.<NewsResponse>builder()
                .result(newsService.getNewsById(id))
                .build();
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_admin')")
    public ApiResponse<NewsResponse> createNews(@RequestBody NewsRequest request) {
        // Debug: Log user authorities
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User authorities: {}", auth.getAuthorities());
        log.info("User principal: {}", auth.getPrincipal());

        return ApiResponse.<NewsResponse>builder()
                .result(newsService.createNews(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_admin')")
    public ApiResponse<NewsResponse> updateNews(@PathVariable String id, @RequestBody NewsRequest request) {
        return ApiResponse.<NewsResponse>builder()
                .result(newsService.updateNews(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_admin')")
    public ApiResponse<String> deleteNews(@PathVariable String id) {
        newsService.deleteNews(id);
        return ApiResponse.<String>builder().result("News deleted").build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_admin')")
    public ApiResponse<NewsResponse> uploadNewsImage(
            @PathVariable String id, @RequestParam("image") MultipartFile image) {
        // Delegate upload to the service which handles Azure storage and updating the News record
        return ApiResponse.<NewsResponse>builder()
                .result(newsService.uploadNewsImage(id, image))
                .build();
    }
}
