package com.hls.sunflower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerRequest {
    private String title;
    private boolean active;
    private String imageUrl; // optional; can be filled after uploading
}
