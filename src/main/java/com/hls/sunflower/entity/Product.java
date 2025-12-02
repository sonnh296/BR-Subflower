package com.hls.sunflower.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "product")
@Data
@EqualsAndHashCode(exclude = {"productImages"})
@ToString(exclude = {"productImages"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;

    private Integer quantity;

    private String size;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

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
