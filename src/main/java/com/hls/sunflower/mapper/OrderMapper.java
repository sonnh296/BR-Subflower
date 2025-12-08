package com.hls.sunflower.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hls.sunflower.dto.response.OrderItemResponse;
import com.hls.sunflower.dto.response.OrderResponse;
import com.hls.sunflower.entity.Order;
import com.hls.sunflower.entity.OrderItem;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductVariant;

@Mapper(componentModel = "spring")
public abstract class OrderMapper {
    @Mapping(target = "orderItems", expression = "java(mapOrderItems(order))")
    public abstract OrderResponse toOrderResponse(Order order);

    protected List<OrderItemResponse> mapOrderItems(Order order) {
        if (order.getOrderItems() == null) {
            return null;
        }
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItemResponses.add(toOrderItemResponse(orderItem));
        }
        return orderItemResponses;
    }

    protected OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        Product product = orderItem.getProduct();
        ProductVariant variant = orderItem.getProductVariant();

        String thumbnailUrl = "/noavatar.png";
        if (product != null
                && product.getProductImages() != null
                && !product.getProductImages().isEmpty()) {
            thumbnailUrl = product.getProductImages().get(0).getImageUrl();
        }

        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .variantId(variant != null ? variant.getId() : null)
                .size(
                        variant != null && variant.getSize() != null
                                ? variant.getSize().getName()
                                : null)
                .quantity(orderItem.getQuantity())
                .priceAtOrder(orderItem.getPriceAtOrder())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
