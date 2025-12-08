package com.hls.sunflower.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(1008, "Invalid request", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTED(2001, "Product not existed", HttpStatus.NOT_FOUND),
    PRODUCT_ITEM_NOT_EXISTED(2002, "Product item not existed", HttpStatus.NOT_FOUND),
    CART_NOT_EXISTED(3001, "Cart not existed", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_EXISTED(4001, "Cart item not existed", HttpStatus.NOT_FOUND),
    PRODUCT_OUT_OF_STOCK(4002, "larger quantity than stock product", HttpStatus.BAD_REQUEST),
    ORDER_NOT_EXISTED(5001, "Order not existed", HttpStatus.NOT_FOUND),
    CART_IS_EMPTY(5002, "Cart is empty", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_BE_CANCELLED(5003, "Order cannot be cancelled", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS_TRANSITION(5004, "Invalid order status transition", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(6001, "Resource not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(7001, "Email already exists", HttpStatus.BAD_REQUEST),
    INVALID_VERIFICATION_TOKEN(7002, "Invalid verification token", HttpStatus.BAD_REQUEST),
    VERIFICATION_TOKEN_EXPIRED(7003, "Verification token expired", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_VERIFIED(7004, "Email already verified", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(7005, "Email not verified. Please verify your email first", HttpStatus.FORBIDDEN);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
