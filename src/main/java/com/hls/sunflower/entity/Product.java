package com.hls.sunflower.entity;

import java.util.ArrayList;
import java.util.List;
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
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JsonIgnore
    private Set<ProductItem> productItem;

    // Helper methods to manage bidirectional relationship
    public void addProductImage(ProductImage image) {
        productImages.add(image);
        image.setProduct(this);
    }

    public void removeProductImage(ProductImage image) {
        productImages.remove(image);
        image.setProduct(null);
    }
}
