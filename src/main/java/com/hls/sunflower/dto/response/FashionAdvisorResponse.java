package com.hls.sunflower.dto.response;

import java.util.List;

public class FashionAdvisorResponse {
    private String recommendation;
    private String userCountry;
    private String userLocation;
    private List<String> suggestedColors;
    private List<String> suggestedStyles;
    private String bodyType;
    private String additionalTips;

    public FashionAdvisorResponse() {}

    public FashionAdvisorResponse(
            String recommendation,
            String userCountry,
            String userLocation,
            List<String> suggestedColors,
            List<String> suggestedStyles,
            String bodyType,
            String additionalTips) {
        this.recommendation = recommendation;
        this.userCountry = userCountry;
        this.userLocation = userLocation;
        this.suggestedColors = suggestedColors;
        this.suggestedStyles = suggestedStyles;
        this.bodyType = bodyType;
        this.additionalTips = additionalTips;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public List<String> getSuggestedColors() {
        return suggestedColors;
    }

    public void setSuggestedColors(List<String> suggestedColors) {
        this.suggestedColors = suggestedColors;
    }

    public List<String> getSuggestedStyles() {
        return suggestedStyles;
    }

    public void setSuggestedStyles(List<String> suggestedStyles) {
        this.suggestedStyles = suggestedStyles;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getAdditionalTips() {
        return additionalTips;
    }

    public void setAdditionalTips(String additionalTips) {
        this.additionalTips = additionalTips;
    }
}
