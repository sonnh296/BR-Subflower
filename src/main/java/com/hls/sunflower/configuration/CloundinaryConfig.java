package com.hls.sunflower.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloundinaryConfig {
    @Bean
    public Cloudinary getCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", "cloud1412");
        config.put("api_key", "349769129783314");
        config.put("api_secret", "qnBCxuVfG-NuB9quoFixPcvEEKY");
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
