package com.hls.sunflower.dao;

import com.hls.sunflower.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByRoleName(String roleName);

}