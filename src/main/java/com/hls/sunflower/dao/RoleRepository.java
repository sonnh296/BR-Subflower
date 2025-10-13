package com.hls.sunflower.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hls.sunflower.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByRoleName(String roleName);
}
