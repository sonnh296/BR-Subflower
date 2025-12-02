package com.hls.sunflower.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    String id;
    ProductItemResponse productItem;
    Integer quantity;
    Double priceAtOrder;
}
