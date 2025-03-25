package com.marche.place.Marche.dto;

import com.marche.place.Marche.enums.PaymentMethod;
import com.marche.place.Marche.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDto {

    private Long id;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;  // 🛑 Vérification du nom du champ
    private PaymentStatus status;
    private Long orderId;
    private LocalDateTime createdAt;
}
