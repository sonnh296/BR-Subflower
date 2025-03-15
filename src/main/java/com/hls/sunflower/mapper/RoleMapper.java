package com.hls.sunflower.mapper;

import com.hls.sunflower.dto.response.RoleResponse;
import com.hls.sunflower.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);
}
