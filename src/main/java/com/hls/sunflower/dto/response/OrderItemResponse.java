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
    String productId;
    String productName;
    String variantId;
    String size;
    Integer quantity;
    Double priceAtOrder;
    String thumbnailUrl;
}
