// ...new file...
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
@Table(name = "product_option")
@Data
@EqualsAndHashCode(exclude = {"product", "sizes"})
@ToString(exclude = {"product", "sizes"})
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_option_size",
            joinColumns = @JoinColumn(name = "product_option_id"),
            inverseJoinColumns = @JoinColumn(name = "size_id"))
    @Builder.Default
    private List<Size> sizes = new ArrayList<>();

    public void addSize(Size size) {
        sizes.add(size);
    }

    public void removeSize(Size size) {
        sizes.remove(size);
    }
}
