package com.hls.sunflower.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private String id;

    @Column
    private String fullName;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    private String avatarUrl;

    @Column
    private Boolean oAuth2;

    @OneToMany(mappedBy = "user",cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private Set<UserRole> user_roles;

    @PrePersist
    public void onCreate() {
        if(oAuth2 == null) {
            oAuth2 = false;
        }
    }
}