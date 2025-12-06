package com.hls.sunflower.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hls.sunflower.dto.request.OrderCreationRequest;
import com.hls.sunflower.dto.request.OrderStatusUpdateRequest;
import com.hls.sunflower.dto.response.ApiResponse;
import com.hls.sunflower.dto.response.OrderResponse;
import com.hls.sunflower.enums.OrderStatus;
import com.hls.sunflower.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreationRequest request) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.createOrder(request));
        return apiResponse;
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getOrderById(orderId));
        return apiResponse;
    }

    @GetMapping("/my-orders")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @RequestParam(name = "field", required = false, defaultValue = "createdAt") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "DESC") String sort) {
        ApiResponse<Page<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getMyOrders(field, pageNumber, pageSize, sort));
        return apiResponse;
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_admin')")
    public ApiResponse<Page<OrderResponse>> getAllOrders(
            @RequestParam(name = "field", required = false, defaultValue = "createdAt") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "DESC") String sort) {
        ApiResponse<Page<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getAllOrders(field, pageNumber, pageSize, sort));
        return apiResponse;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_admin')")
    public ApiResponse<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(name = "field", required = false, defaultValue = "createdAt") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "DESC") String sort) {
        ApiResponse<Page<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.getOrdersByStatus(status, field, pageNumber, pageSize, sort));
        return apiResponse;
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_admin')")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String orderId, @Valid @RequestBody OrderStatusUpdateRequest request) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.updateOrderStatus(orderId, request));
        return apiResponse;
    }

    @DeleteMapping("/{orderId}")
    public ApiResponse<String> cancelOrder(@PathVariable String orderId) {
        orderService.cancelOrder(orderId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Order cancelled successfully");
        return apiResponse;
    }
}
