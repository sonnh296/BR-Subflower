package com.hls.sunflower.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hls.sunflower.dto.response.CartResponse;
import com.hls.sunflower.entity.Cart;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "cartItems", ignore = true)
    CartResponse toCartResponse(Cart cart);
}
