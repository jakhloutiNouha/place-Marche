package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.PaymentDto;
import com.marche.place.Marche.entity.Order;
import com.marche.place.Marche.entity.Payment;
import com.marche.place.Marche.enums.OrderStatus;
import com.marche.place.Marche.enums.PaymentMethod;
import com.marche.place.Marche.enums.PaymentStatus;
import com.marche.place.Marche.repository.OrderRepository;
import com.marche.place.Marche.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Transactional
    public PaymentDto processPayment(PaymentDto paymentDto) {
        // Récupérer la commande associée
        Order order = orderRepository.findById(paymentDto.getOrderId())
                .orElseThrow(() -> new NoSuchElementException("Commande non trouvée"));

        // Vérifier que la commande est en attente
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Impossible de traiter le paiement pour une commande avec le statut: " + order.getStatus());
        }

        // Créer et sauvegarder le paiement
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setCreatedAt(LocalDateTime.now());

        // Simuler le traitement du paiement (dans un vrai système, ceci serait remplacé par un appel à un service de paiement)
        boolean paymentSuccess = simulatePaymentProcessing(paymentDto);

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.COMPLETED);
            // Mettre à jour le statut de la commande
            orderService.updateOrderStatus(order.getId(), OrderStatus.CONFIRMED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDto(savedPayment);
    }

    private boolean simulatePaymentProcessing(PaymentDto paymentDto) {
        // Dans un système réel, cette méthode ferait un appel à une API de paiement
        // Pour l'exemple, on simule une réussite à 95%
        return Math.random() < 0.95;
    }

    private PaymentDto convertToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .orderId(payment.getOrder().getId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}