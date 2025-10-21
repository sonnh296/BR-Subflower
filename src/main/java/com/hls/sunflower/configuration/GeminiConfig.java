package com.hls.sunflower.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class GeminiConfig {

    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    @Bean
    public String geminiApiKey() {
        String apiKey = dotenv.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || "your_gemini_api_key_here".equals(apiKey)) {
            log.warn("GEMINI_API_KEY is not set in environment variables. Fashion advisor features will not work.");
            return ""; // Return empty string instead of throwing exception
        }
        log.info("Gemini API Key configured successfully");
        return apiKey;
    }

    @Bean
    public String geminiModel() {
        String model = dotenv.get("GEMINI_MODEL");
        return model != null && !model.isEmpty() ? model : "gemini-1.5-flash";
    }
}
