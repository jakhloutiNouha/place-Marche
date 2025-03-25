package com.marche.place.Marche.entity;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.marche.place.Marche.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Objects;
@Data
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @ManyToMany
    @JoinTable(
            name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();
}