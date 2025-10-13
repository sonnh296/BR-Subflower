package com.hls.sunflower.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hls.sunflower.dto.request.UserCreationRequest;
import com.hls.sunflower.dto.request.UserUpdateRequest;
import com.hls.sunflower.dto.response.UserResponse;
import com.hls.sunflower.entity.Users;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "user_roles", ignore = true)
    Users toUser(UserCreationRequest request);

    UserResponse toUserResponse(Users user);

    @Mapping(target = "user_roles", ignore = true)
    void updateUser(@MappingTarget Users user, UserUpdateRequest request);
}
