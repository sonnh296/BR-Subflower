package com.hls.sunflower.mapper;

import org.mapstruct.Mapper;

import com.hls.sunflower.dto.response.OrderResponse;
import com.hls.sunflower.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toOrderResponse(Order order);
}
