package com.hls.sunflower.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsResponse {
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
