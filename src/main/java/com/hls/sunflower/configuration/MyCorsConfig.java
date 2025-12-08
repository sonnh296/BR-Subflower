package com.hls.sunflower.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class MyCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép các domain cụ thể
        config.addAllowedOrigin("https://elsun.site");
        config.addAllowedOrigin("https://elsun.vn");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("https://purple-field-00d103000.3.azurestaticapps.net");

        // Hoặc cho phép tất cả các domain (không khuyến khích trong môi trường sản xuất)
        // config.addAllowedOrigin("*");

        // Cho phép các method cụ thể
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Cho phép các header cụ thể
        config.addAllowedHeader("*"); // Allow all headers

        // Expose headers that client can access
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");

        // Cho phép credentials (cookie, authorization headers)
        config.setAllowCredentials(true);

        // Max age for preflight requests
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
