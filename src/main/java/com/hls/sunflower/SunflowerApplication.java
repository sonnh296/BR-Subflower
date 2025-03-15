package com.hls.sunflower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SunflowerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SunflowerApplication.class, args);
	}

}
