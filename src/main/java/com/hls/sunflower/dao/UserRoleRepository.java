package com.hls.sunflower.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hls.sunflower.entity.UserRole;
import com.hls.sunflower.entity.Users;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    UserRole findByUser_IdAndRole_Id(String id, String id1);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user = :user")
    void deleteByUser(Users user);

    @Modifying
    @Query(value = "DELETE ur from user_role as ur where ur.user_id = :userId", nativeQuery = true)
    void deleteByUserId(String userId);
}
