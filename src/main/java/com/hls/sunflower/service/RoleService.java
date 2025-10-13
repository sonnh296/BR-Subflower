package com.hls.sunflower.service;

import java.util.List;

import com.hls.sunflower.dto.response.RoleResponse;

public interface RoleService {
    public List<RoleResponse> getRoles();

    public RoleResponse getById(String id);
}
