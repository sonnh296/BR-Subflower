package com.hls.sunflower.mapper;

import java.sql.Timestamp;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.hls.sunflower.dto.response.CartItemResponse;
import com.hls.sunflower.entity.CartItem;
import com.hls.sunflower.util.TimestampUtil;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Named("timestampToString")
    @Mapping(target = "addedAt", expression = "java(timestampToString(cartItem.getAddedAt()))")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    default String timestampToString(Timestamp timestamp) {
        return TimestampUtil.timestampToString(timestamp);
    }
}
