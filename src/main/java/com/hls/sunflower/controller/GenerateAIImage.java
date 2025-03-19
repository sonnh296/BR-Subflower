package com.hls.sunflower.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/virtual-try-on")
public class GenerateAIImage {

    @Value("${api.access.key}")
    private String accessKey;

    @Value("${api.secret.key}")
    private String secretKey;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    private static final String SUBMIT_ENDPOINT = "/v1/images/kolors-virtual-try-on";
    private static final String STATUS_ENDPOINT = "/v1/images/kolors-virtual-try-on/%s";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> processVirtualTryOn(
            @RequestParam("humanImage") MultipartFile humanImage,
            @RequestParam("clothImage") String clothImage) {

        try {
            // Generate JWT token
            String token = generateToken(accessKey, secretKey);

            // Convert human image to Base64
            String humanImageBase64 = Base64.getEncoder().encodeToString(humanImage.getBytes());

            // For cloth image, we need to either upload it somewhere to get a URL
            // or convert it to Base64 as well based on API requirements
            // For this example, we'll convert cloth image to Base64 too

            // Create request body
            String requestBody = createRequestBody(humanImageBase64, clothImage);

            // Submit the task
            JSONObject submitResponse = submitVirtualTryOnTask(token, requestBody);

            // Check if task submission was successful
            if (submitResponse.getInt("code") != 0) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Error submitting task: " + submitResponse.getString("message"));
            }

            // Extract task_id
            String taskId = submitResponse.getJSONObject("data").getString("task_id");

            // Poll for task completion
            JSONObject resultResponse = pollTaskCompletion(token, taskId);

            if (resultResponse.getInt("code") == 0 &&
                    resultResponse.getJSONObject("data").getString("task_status").equals("succeed")) {

                // Extract the first image URL (assuming we want the first one)
                JSONArray images = resultResponse.getJSONObject("data")
                        .getJSONObject("task_result")
                        .getJSONArray("images");

                if (images.length() > 0) {
                    String imageUrl = images.getJSONObject(0).getString("url");

                    // Create response with image URL
                    Map<String, String> response = new HashMap<>();
                    response.put("imageUrl", imageUrl);

                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("No images generated");
                }
            } else {
                String errorMsg = "Task failed: ";
                if (resultResponse.getJSONObject("data").has("task_status_msg")) {
                    errorMsg += resultResponse.getJSONObject("data").getString("task_status_msg");
                } else {
                    errorMsg += "Unknown error";
                }

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    private String generateToken(String ak, String sk) {
        try {
            Date expiredAt = new Date(System.currentTimeMillis() + 1800*1000); // Valid time: current time + 1800s (30min)
            Date notBefore = new Date(System.currentTimeMillis() - 5*1000); // Start time: current time - 5s
            Algorithm algo = Algorithm.HMAC256(sk);
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("alg", "HS256");
            return JWT.create()
                    .withIssuer(ak)
                    .withHeader(header)
                    .withExpiresAt(expiredAt)
                    .withNotBefore(notBefore)
                    .sign(algo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createRequestBody(String humanImageBase64, String clothImageBase64) {
        // Note: Adjust this method if the API requires cloth_image as URL instead of Base64
        return String.format("{\"human_image\":\"%s\",\"cloth_image\":\"%s\"}",
                humanImageBase64, clothImageBase64);
    }

    private JSONObject submitVirtualTryOnTask(String token, String requestBody) throws Exception {
        URL url = new URL(apiBaseUrl + SUBMIT_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up HTTP request
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setDoOutput(true);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get response
        String response = readResponse(connection);
        return new JSONObject(response);
    }

    private JSONObject getTaskStatus(String token, String taskId) throws Exception {
        URL url = new URL(apiBaseUrl + String.format(STATUS_ENDPOINT, taskId));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up HTTP request
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        // Get response
        String response = readResponse(connection);
        return new JSONObject(response);
    }

    private JSONObject pollTaskCompletion(String token, String taskId) throws Exception {
        final int MAX_RETRIES = 30;
        final int RETRY_INTERVAL = 2000;

        JSONObject response = null;

        for (int i = 0; i < MAX_RETRIES; i++) {
            response = getTaskStatus(token, taskId);

            if (response.getInt("code") != 0) {
                break;
            }

            String taskStatus = response.getJSONObject("data").getString("task_status");

            if (taskStatus.equals("succeed") || taskStatus.equals("failed")) {
                break;
            }

            Thread.sleep(RETRY_INTERVAL);
        }

        return response;
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        StringBuilder response = new StringBuilder();
        int responseCode = connection.getResponseCode();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode >= 300 ? connection.getErrorStream() : connection.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }
}
