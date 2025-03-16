package com.hls.sunflower.service.serviceImpl;

import com.hls.sunflower.dao.RoleRepository;
import com.hls.sunflower.dto.response.RoleResponse;
import com.hls.sunflower.mapper.RoleMapper;
import com.hls.sunflower.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public RoleResponse getById(String id) {
        return roleMapper.toRoleResponse(roleRepository.findById(id).get());
    }

    @Override
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

}
