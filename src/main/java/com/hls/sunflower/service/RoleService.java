package com.hls.sunflower.service;


import com.hls.sunflower.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    public List<RoleResponse> getRoles();

    public RoleResponse getById(String id);
}
