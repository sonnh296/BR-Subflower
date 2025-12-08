package com.hls.sunflower.entity;

import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
@Data
@EqualsAndHashCode(exclude = {"user_roles", "cart"})
@ToString(exclude = {"user_roles", "cart"})
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

    @Column
    private String phoneNumber;

    private String avatarUrl;

    @Column
    private Boolean oAuth2;

    @Column
    private Boolean emailVerified;

    @Column
    private String verificationToken;

    @Column
    private java.time.LocalDateTime verificationTokenExpiry;

    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private Set<UserRole> user_roles;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private Cart cart;

    @PrePersist
    public void onCreate() {
        if (oAuth2 == null) {
            oAuth2 = false;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
    }
}
