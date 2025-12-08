package com.hls.sunflower.entity;

import java.time.LocalDateTime;
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
@EqualsAndHashCode(exclude = {"productImages", "variants", "productOptions"})
@ToString(exclude = {"productImages", "variants", "productOptions"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    // Availability dates
    private LocalDateTime availableFrom;
    private LocalDateTime availableTo;

    // New: category relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // New: product options (e.g., Full set, Top only, Skirt only)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductOption> productOptions = new ArrayList<>();

    // Helper methods to manage bidirectional relationship
    public void addProductImage(ProductImage image) {
        productImages.add(image);
        image.setProduct(this);
    }

    public void removeProductImage(ProductImage image) {
        productImages.remove(image);
        image.setProduct(null);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public void addProductOption(ProductOption option) {
        productOptions.add(option);
        option.setProduct(this);
    }

    public void removeProductOption(ProductOption option) {
        productOptions.remove(option);
        option.setProduct(null);
    }
}
