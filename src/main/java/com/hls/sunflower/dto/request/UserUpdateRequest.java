package com.hls.sunflower.dto.request;

import com.hls.sunflower.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String fullName;

    private String password;

    private String email;

    private String avatarUrl;
}
