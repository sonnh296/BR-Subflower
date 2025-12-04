package com.hls.sunflower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsRequest {
    private String title;
    private String content;
    private Boolean published; // Changed to Boolean wrapper to handle null values
    private String imageUrl; // optional, can be set after uploading
}
