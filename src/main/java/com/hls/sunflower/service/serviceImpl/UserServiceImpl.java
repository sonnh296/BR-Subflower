package com.hls.sunflower.service.serviceImpl;

import com.hls.sunflower.dao.RoleRepository;
import com.hls.sunflower.dao.UserRoleRepository;
import com.hls.sunflower.dao.UsersRepository;
import com.hls.sunflower.dto.request.UserCreationRequest;
import com.hls.sunflower.dto.request.UserUpdateRequest;
import com.hls.sunflower.dto.response.UserResponse;
import com.hls.sunflower.entity.UserRole;
import com.hls.sunflower.entity.Users;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.mapper.UserMapper;
import com.hls.sunflower.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getById(String id) {
        return userMapper.toUserResponse(usersRepository.findById(id).get());
    }

    @Override
    public UserResponse getByUsername(String username) {
        return userMapper.toUserResponse(usersRepository.findByUsername(username).get());
    }

    @Override
    public UserResponse addUser(UserCreationRequest request) {
        if(usersRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);


        Users user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //them role User
        Set<UserRole> user_roles = new HashSet<>();
        UserRole user_role = new UserRole();
        user_role.setRole(roleRepository.findByRoleName("USER"));
        user_role.setUser(user);
        user_roles.add(user_role);

        user.setUser_roles(user_roles);

        return userMapper.toUserResponse(usersRepository.save(user));
    }

    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return usersRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    @Override
    public Page<UserResponse> getUsersContains(String s, Pageable pageable) {
        return usersRepository.findByUsernameContainsIgnoreCase(s, pageable).map(userMapper::toUserResponse);
    }

    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Users user = usersRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        Users user = usersRepository.findById(userId).get();
        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // xoá user id trong user_role
        userRoleRepository.deleteByUser(user);

        return userMapper.toUserResponse(usersRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        userRoleRepository.deleteByUserId(userId);
        usersRepository.deleteById(userId);
    }
}
