package com.marche.place.Marche.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
    private String productName;
    private String imageUrl;
}