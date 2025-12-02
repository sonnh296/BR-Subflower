package com.hls.sunflower.dto.request;

import com.hls.sunflower.enums.OrderStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class OrderStatusUpdateRequest {
    OrderStatus status;
}
