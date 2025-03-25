// OrderItemRepository.java
package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);
    Page<OrderItem> findByProductId(Long productId, Pageable pageable);
}