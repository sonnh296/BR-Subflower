package com.hls.sunflower.enums;

public enum OrderStatus {
    PLACED, // Order placed by user (no online payment)
    PENDING, // Order created, waiting for admin review (kept for backward compatibility)
    CONFIRMED, // Admin confirmed the order
    PROCESSING, // Order is being prepared
    SHIPPING, // Order is out for delivery
    DELIVERED, // Order has been delivered
    CANCELLED // Order was cancelled
}
