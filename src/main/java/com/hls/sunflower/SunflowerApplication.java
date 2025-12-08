package com.hls.sunflower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import com.hls.sunflower.config.ProductMapperConfig;

@SpringBootApplication
@EnableFeignClients
@Import(ProductMapperConfig.class)
public class SunflowerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SunflowerApplication.class, args);
    }
}
