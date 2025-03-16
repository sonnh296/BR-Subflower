package com.hls.sunflower.mapper;

import com.hls.sunflower.dto.response.CartResponse;
import com.hls.sunflower.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "cartItems", ignore = true)
    CartResponse toCartResponse(Cart cart);
}
