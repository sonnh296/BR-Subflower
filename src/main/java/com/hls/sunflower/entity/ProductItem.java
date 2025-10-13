package com.hls.sunflower.entity;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "product_item")
public class ProductItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private double price;

    private String category;

    private String gender;

    private String size;

    private String color;

    private int stockQuantity;

    private boolean isActive;

    private String url;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(mappedBy = "productItem")
    @JsonIgnore
    private CartItem cartItem;
}
