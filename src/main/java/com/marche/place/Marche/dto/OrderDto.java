package com.marche.place.Marche.dto;

import com.marche.place.Marche.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Long customerId;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items = new ArrayList<>();

    // Ajouter pour les informations d'expédition
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostalCode;
    private String contactEmail;
    private String contactPhone;

    // Informations clients
    private String firstName;
    private String lastName;
}