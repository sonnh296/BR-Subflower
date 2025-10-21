package com.hls.sunflower.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionAdvisorRequest {
    @NotBlank(message = "Skin color is required")
    private String skinColor;

    @NotBlank(message = "Bust measurement is required")
    private String bust;

    @NotBlank(message = "Waist measurement is required")
    private String waist;

    @NotBlank(message = "Hip measurement is required")
    private String hip;

    private String height;
    private String weight;
    private String style; // casual, formal, sporty, elegant, etc.
    private String occasion; // daily, party, wedding, work, etc.
    private String additionalInfo; // any additional preferences or information
}
