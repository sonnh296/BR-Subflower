package com.hls.sunflower.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.hls.sunflower.enums.OrderStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String id;
    UserResponse user;
    List<OrderItemResponse> orderItems;
    Double totalPrice;
    OrderStatus status;
    String deliveryAddress;
    String phoneNumber;
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
