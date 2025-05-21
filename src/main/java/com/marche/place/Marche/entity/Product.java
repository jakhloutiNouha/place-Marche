package com.marche.place.Marche.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marche.place.Marche.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private User vendor;

    @ManyToMany(mappedBy = "products")
    private List<Promotion> promotions;

    // Méthodes utilitaires
    @Transient
    public Long getStoreId() {
        return store != null ? store.getId() : null;
    }

    @Transient
    public String getStoreName() {
        return store != null ? store.getTitle() : null;
    }

    // Ajout de la méthode getVendorId()
    @Transient
    public Long getVendorId() {
        return vendor != null ? vendor.getId() : null;
    }

    @Transient
    public BigDecimal getDiscountedPrice() {
        if (promotions == null || promotions.isEmpty()) {
            return price;
        }
        LocalDateTime now = LocalDateTime.now();
        return promotions.stream()
                .filter(promo ->
                        promo.getStartDate().isBefore(now)
                                && promo.getEndDate().isAfter(now)
                                && promo.getDiscountType() == DiscountType.PERCENTAGE)
                .findFirst()
                .map(promo -> price.subtract(price.multiply(promo.getDiscountValue().divide(BigDecimal.valueOf(100)))))
                .orElse(price);
    }

    public String getName() {
        return this.title;
    }
}