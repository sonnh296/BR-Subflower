package com.hls.sunflower.mapper;

import org.mapstruct.Mapper;

import com.hls.sunflower.dto.response.OrderItemResponse;
import com.hls.sunflower.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
