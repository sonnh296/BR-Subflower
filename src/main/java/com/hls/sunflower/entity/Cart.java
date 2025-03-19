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
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(
            mappedBy = "cart",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private Set<CartItem> cartItems;
}
