package com.marche.place.Marche.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor  // ✅ Ajoute automatiquement un constructeur avec les 3 paramètres
public class OrderItemDto {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
