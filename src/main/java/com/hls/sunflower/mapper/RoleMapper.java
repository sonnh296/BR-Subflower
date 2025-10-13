package com.hls.sunflower.mapper;

import org.mapstruct.Mapper;

import com.hls.sunflower.dto.response.RoleResponse;
import com.hls.sunflower.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);
}
