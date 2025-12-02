package com.hls.sunflower.service.serviceImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.hls.sunflower.dto.request.FashionAdvisorRequest;
import com.hls.sunflower.dto.response.FashionAdvisorResponse;
import com.hls.sunflower.service.FashionAdvisorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FashionAdvisorServiceImpl implements FashionAdvisorService {

    private final String geminiApiKey;
    private final String geminiModel;

    @Override
    public FashionAdvisorResponse getFashionRecommendation(
            FashionAdvisorRequest request, HttpServletRequest httpRequest) {
        // Declare location and country outside try block so they're accessible in catch blocks
        String location = "Unknown Location";
        String country = "Unknown";

        // Check if API key is configured
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.error("Gemini API key is not configured");
            return createErrorResponse(
                    "Fashion advisor service is not configured. Please contact the administrator.", location, country);
        }

        try {
            // Get user location
            location = getUserLocationFromRequest(httpRequest);
            country = extractCountryFromLocation(location);

            // Build the system prompt
            String systemPrompt = buildSystemPrompt();

            // Build the user message
            String userMessage = buildUserMessage(request, location, country);

            // Call Gemini API
            String aiResponse = callGeminiApi(systemPrompt + "\n\n" + userMessage);

            // Parse the AI response
            return parseAIResponse(aiResponse, location, country);

        } catch (Exception e) {
            log.error("Error getting fashion recommendation: ", e);
            String errorMessage = "An unexpected error occurred. Please try again later.";

            if (e.getMessage() != null) {
                if (e.getMessage().contains("429")) {
                    errorMessage = "The AI service has reached its usage limit. Please try again later.";
                } else if (e.getMessage().contains("401") || e.getMessage().contains("403")) {
                    errorMessage = "AI service authentication failed. Please check the API key configuration.";
                } else if (e.getMessage().contains("500") || e.getMessage().contains("503")) {
                    errorMessage = "The AI service is temporarily unavailable. Please try again in a few minutes.";
                }
            }

            return createErrorResponse(errorMessage, location, country);
        }
    }

    private String callGeminiApi(String prompt) throws Exception {
        String apiUrl = "https://generativelanguage.googleapis.com/v1/models/" + geminiModel + ":generateContent?key="
                + geminiApiKey;

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000); // 30 seconds
        conn.setReadTimeout(30000);

        // Build request body
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            log.error("Gemini API error ({}): {}", responseCode, errorResponse.toString());
            throw new RuntimeException("Gemini API error: " + responseCode + " - " + errorResponse.toString());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        // Parse Gemini response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray candidates = jsonResponse.getJSONArray("candidates");
        if (candidates.length() > 0) {
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject contentObj = candidate.getJSONObject("content");
            JSONArray partsArray = contentObj.getJSONArray("parts");
            if (partsArray.length() > 0) {
                return partsArray.getJSONObject(0).getString("text");
            }
        }

        throw new RuntimeException("No content in Gemini response");
    }

    private String buildSystemPrompt() {
        return """
				Bạn là chuyên gia tư vấn thời trang chuyên nghiệp với kiến thức về dáng người,
				lý thuyết màu sắc và xu hướng thời trang. Hãy đưa ra lời khuyên ngắn gọn, súc tích.

				Trả lời bằng tiếng Việt theo định dạng JSON sau:
				{
				"recommendation": "Lời khuyên chính ngắn gọn (100-150 từ) viết bằng MARKDOWN với các heading, bullet points, bold text",
				"suggestedColors": ["màu1", "màu2", "màu3", "màu4"],
				"suggestedStyles": ["phong cách1", "phong cách2", "phong cách3"],
				"bodyType": "Dáng người (đồng hồ cát, quả lê, quả táo, chữ nhật, tam giác ngược)",
				"additionalTips": "Gợi ý thêm ngắn gọn"
				}

				Lưu ý:
				- Trả lời NGẮN GỌN và DỄ HIỂU
				- Sử dụng Markdown trong phần "recommendation" (## heading, **bold**, bullet points)
				- Tập trung vào những gì phù hợp với dáng người và tông da
				- Xem xét khí hậu và văn hóa của khu vực người dùng
				""";
    }

    private String buildUserMessage(FashionAdvisorRequest request, String location, String country) {
        StringBuilder message = new StringBuilder();
        message.append("Tôi cần tư vấn thời trang dựa trên số đo và sở thích của tôi:\n\n");
        message.append("Số đo cơ thể:\n");
        message.append("- Vòng ngực: ").append(request.getBust()).append(" cm\n");
        message.append("- Vòng eo: ").append(request.getWaist()).append(" cm\n");
        message.append("- Vòng mông: ").append(request.getHip()).append(" cm\n");

        if (request.getHeight() != null && !request.getHeight().isEmpty()) {
            message.append("- Chiều cao: ").append(request.getHeight()).append(" cm\n");
        }
        if (request.getWeight() != null && !request.getWeight().isEmpty()) {
            message.append("- Cân nặng: ").append(request.getWeight()).append(" kg\n");
        }

        message.append("\nMàu da: ").append(request.getSkinColor()).append("\n");

        if (request.getStyle() != null && !request.getStyle().isEmpty()) {
            message.append("Phong cách yêu thích: ").append(request.getStyle()).append("\n");
        }
        if (request.getOccasion() != null && !request.getOccasion().isEmpty()) {
            message.append("Dịp: ").append(request.getOccasion()).append("\n");
        }
        if (request.getAdditionalInfo() != null && !request.getAdditionalInfo().isEmpty()) {
            message.append("Thông tin thêm: ")
                    .append(request.getAdditionalInfo())
                    .append("\n");
        }

        message.append("\nVị trí của tôi: ").append(location);
        if (country != null && !country.isEmpty()) {
            message.append(" (").append(country).append(")");
        }
        message.append("\n\nHãy gợi ý cho tôi những trang phục phù hợp, ");
        message.append("xem xét dáng người, màu da và khí hậu/xu hướng thời trang địa phương. ");
        message.append("Hãy trả lời NGẮN GỌN và sử dụng Markdown format.");

        return message.toString();
    }

    private FashionAdvisorResponse parseAIResponse(String aiResponse, String location, String country) {
        try {
            // Try to extract JSON from the response
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (aiResponse.contains("{")) {
                int startIndex = aiResponse.indexOf("{");
                int endIndex = aiResponse.lastIndexOf("}") + 1;
                if (startIndex >= 0 && endIndex > startIndex) {
                    jsonStr = aiResponse.substring(startIndex, endIndex);
                }
            }

            JSONObject json = new JSONObject(jsonStr);

            List<String> suggestedColors = new ArrayList<>();
            if (json.has("suggestedColors")) {
                JSONArray colorsArray = json.getJSONArray("suggestedColors");
                for (int i = 0; i < colorsArray.length(); i++) {
                    suggestedColors.add(colorsArray.getString(i));
                }
            }

            List<String> suggestedStyles = new ArrayList<>();
            if (json.has("suggestedStyles")) {
                JSONArray stylesArray = json.getJSONArray("suggestedStyles");
                for (int i = 0; i < stylesArray.length(); i++) {
                    suggestedStyles.add(stylesArray.getString(i));
                }
            }

            FashionAdvisorResponse response = new FashionAdvisorResponse();
            response.setRecommendation(json.optString("recommendation", aiResponse));
            response.setUserCountry(country);
            response.setUserLocation(location);
            response.setSuggestedColors(suggestedColors);
            response.setSuggestedStyles(suggestedStyles);
            response.setBodyType(json.optString("bodyType", ""));
            response.setAdditionalTips(json.optString("additionalTips", ""));
            return response;

        } catch (Exception e) {
            log.error("Error parsing AI response, using raw response: ", e);
            // Fallback: return the raw AI response
            FashionAdvisorResponse response = new FashionAdvisorResponse();
            response.setRecommendation(aiResponse);
            response.setUserCountry(country);
            response.setUserLocation(location);
            response.setSuggestedColors(new ArrayList<>());
            response.setSuggestedStyles(new ArrayList<>());
            response.setBodyType("");
            response.setAdditionalTips("");
            return response;
        }
    }

    @Override
    public String getUserLocationFromRequest(HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            log.info("Detecting location for IP: {}", ipAddress);

            // Skip location detection for localhost/testing - return default
            if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress) || "8.8.8.8".equals(ipAddress)) {
                log.info("Localhost detected, skipping IP geolocation");
                return "Local Development Environment";
            }

            // Use ipapi.co for IP geolocation (free tier available)
            String urlString = "https://ipapi.co/" + ipAddress + "/json/";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000); // 5 second timeout
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 429) {
                log.warn("IP geolocation API rate limit exceeded, using fallback");
                return "Unknown Location (Rate Limit)";
            }

            if (responseCode != 200) {
                log.warn("IP geolocation API returned status: {}", responseCode);
                return "Unknown Location";
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            String city = json.optString("city", "Unknown");
            String countryName = json.optString("country_name", "Unknown");
            String region = json.optString("region", "");

            String location = city;
            if (!region.isEmpty() && !region.equals(city)) {
                location += ", " + region;
            }
            location += ", " + countryName;

            log.info("Detected location: {}", location);
            return location;

        } catch (java.net.SocketTimeoutException e) {
            log.warn("Location detection timeout: {}", e.getMessage());
            return "Unknown Location (Timeout)";
        } catch (java.io.IOException e) {
            if (e.getMessage().contains("429")) {
                log.warn("IP geolocation API rate limit exceeded");
                return "Unknown Location (Rate Limit)";
            }
            log.warn("Error detecting location: {}", e.getMessage());
            return "Unknown Location";
        } catch (Exception e) {
            log.error("Unexpected error detecting location: ", e);
            return "Unknown Location";
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs in X-Forwarded-For
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        // If localhost, use a default IP for testing
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "8.8.8.8"; // Default to US for testing
        }
        return remoteAddr;
    }

    private String extractCountryFromLocation(String location) {
        if (location == null
                || location.equals("Unknown Location")
                || location.contains("Rate Limit")
                || location.contains("Timeout")) {
            return "Unknown";
        }
        String[] parts = location.split(",");
        return parts.length > 0 ? parts[parts.length - 1].trim() : "Unknown";
    }

    private FashionAdvisorResponse createErrorResponse(String errorMessage, String location, String country) {
        FashionAdvisorResponse response = new FashionAdvisorResponse();
        response.setRecommendation(errorMessage);
        response.setUserCountry(country);
        response.setUserLocation(location);
        response.setSuggestedColors(new ArrayList<>());
        response.setSuggestedStyles(new ArrayList<>());
        response.setBodyType("");
        response.setAdditionalTips("");
        return response;
    }
}
