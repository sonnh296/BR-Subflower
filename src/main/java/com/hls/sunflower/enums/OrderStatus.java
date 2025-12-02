package com.hls.sunflower.enums;

public enum OrderStatus {
    PENDING, // Order created, waiting for admin review
    CONFIRMED, // Admin confirmed the order
    PROCESSING, // Order is being prepared
    SHIPPING, // Order is out for delivery
    DELIVERED, // Order has been delivered
    CANCELLED // Order was cancelled
}
