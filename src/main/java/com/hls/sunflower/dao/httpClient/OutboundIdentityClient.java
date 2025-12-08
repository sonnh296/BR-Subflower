package com.hls.sunflower.dao.httpClient;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hls.sunflower.configuration.FeignConfig;

import feign.Response;

@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com", configuration = FeignConfig.class)
public interface OutboundIdentityClient {
    // Use @RequestBody with a Map so we can guarantee exact field names (snake_case) required by Google's token
    // endpoint. Return raw feign.Response so the caller can safely read status, headers and body bytes.
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Response exchangeToken(@RequestBody Map<String, ?> form);
}
