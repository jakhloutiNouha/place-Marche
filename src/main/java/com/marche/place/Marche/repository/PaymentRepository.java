package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Payment;
import com.marche.place.Marche.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
}
