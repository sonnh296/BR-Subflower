package com.hls.sunflower.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private int quantity;

    private Timestamp addedAt;

    @OneToOne
    @JoinColumn(name = "product_item_id")
    @JsonIgnore
    private ProductItem productItem;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @PrePersist
    public void prePersist() {
        this.addedAt = new Timestamp(System.currentTimeMillis());
    }
}
