package com.hls.sunflower.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hls.sunflower.dto.request.UserCreationRequest;
import com.hls.sunflower.dto.request.UserUpdateRequest;
import com.hls.sunflower.dto.response.UserResponse;

public interface UserService {
    public Page<UserResponse> getUsers(Pageable pageable);

    public Page<UserResponse> getUsersContains(String s, Pageable pageable);

    public UserResponse getById(String id);

    public UserResponse getByUsername(String username);

    public UserResponse addUser(UserCreationRequest request);

    public UserResponse updateUser(String userId, UserUpdateRequest request);

    public void deleteUser(String userId);

    public UserResponse getMyInfo();
}
