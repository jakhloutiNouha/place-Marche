package com.marche.place.Marche.dto;

import com.marche.place.Marche.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor  // ✅ Génère un constructeur avec TOUS les paramètres
@NoArgsConstructor   // ✅ Génère un constructeur vide (nécessaire pour la sérialisation)
public class OrderDto {
    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Long customerId;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
}
