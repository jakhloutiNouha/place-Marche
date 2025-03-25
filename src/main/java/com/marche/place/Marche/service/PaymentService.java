package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.PaymentDto;
import com.marche.place.Marche.entity.Order;
import com.marche.place.Marche.entity.Payment;
import com.marche.place.Marche.enums.PaymentMethod;
import com.marche.place.Marche.enums.PaymentStatus;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.OrderRepository;
import com.marche.place.Marche.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentDto createPayment(PaymentDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));

        Payment payment = Payment.builder()
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())  // 🛑 Vérification du nom du champ
                .status(PaymentStatus.PENDING)
                .order(order)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDto(savedPayment);
    }

    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));
        return convertToDto(payment);
    }

    public Page<PaymentDto> getPaymentsByMethod(PaymentMethod method, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByPaymentMethod(method, pageable);
        return payments.map(this::convertToDto);
    }

    private PaymentDto convertToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())  // 🛑 Vérification du nom du champ
                .status(payment.getStatus())
                .orderId(payment.getOrder().getId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
