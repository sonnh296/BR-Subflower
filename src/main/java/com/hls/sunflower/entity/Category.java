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
@Table(name = "category")
@Data
@EqualsAndHashCode(exclude = {"sizes"})
@ToString(exclude = {"sizes"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Size> sizes = new ArrayList<>();

    public void addSize(Size size) {
        sizes.add(size);
        size.setCategory(this);
    }

    public void removeSize(Size size) {
        sizes.remove(size);
        size.setCategory(null);
    }
}
