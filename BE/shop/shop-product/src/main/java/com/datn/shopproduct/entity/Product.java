package com.datn.shopproduct.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"images", "category"})
@Entity
@Table(name = "PRODUCTS")
public class Product  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(nullable = false, unique = true, length = 120)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    // Giá bán
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Giá nhập
    @Column(name = "import_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal importPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;
}


