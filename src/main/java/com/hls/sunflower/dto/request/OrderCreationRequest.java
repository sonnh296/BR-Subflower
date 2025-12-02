package com.hls.sunflower.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class OrderCreationRequest {
    @NotBlank(message = "Delivery address is required")
    String deliveryAddress;

    @NotBlank(message = "Phone number is required")
    String phoneNumber;

    String notes;
}
