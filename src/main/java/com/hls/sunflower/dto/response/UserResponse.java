package com.hls.sunflower.dto.response;

import com.hls.sunflower.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;

    private String fullName;

    private String username;

    private String email;

    private String avatarUrl;

    private Set<UserRole> user_roles;
}
