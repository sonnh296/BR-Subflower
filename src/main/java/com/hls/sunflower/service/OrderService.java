package com.hls.sunflower.service;

import org.springframework.data.domain.Page;

import com.hls.sunflower.dto.request.OrderCreationRequest;
import com.hls.sunflower.dto.request.OrderStatusUpdateRequest;
import com.hls.sunflower.dto.response.OrderResponse;
import com.hls.sunflower.enums.OrderStatus;

public interface OrderService {
    OrderResponse createOrder(OrderCreationRequest request);

    OrderResponse getOrderById(String orderId);

    Page<OrderResponse> getMyOrders(String field, Integer pageNumber, Integer pageSize, String sort);

    Page<OrderResponse> getAllOrders(String field, Integer pageNumber, Integer pageSize, String sort);

    Page<OrderResponse> getOrdersByStatus(
            OrderStatus status, String field, Integer pageNumber, Integer pageSize, String sort);

    OrderResponse updateOrderStatus(String orderId, OrderStatusUpdateRequest request);

    void cancelOrder(String orderId);
}
