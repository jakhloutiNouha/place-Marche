package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Payment;
import com.marche.place.Marche.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);
}