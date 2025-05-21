package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);

    // Correction de la requête pour utiliser vendor.id au lieu de vendorId
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi JOIN oi.product p JOIN p.vendor v WHERE v.id = :vendorId")
    List<Order> findByVendorId(@Param("vendorId") Long vendorId);

    // Correction de la requête pour les produits spécifiques
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.id IN :productIds")
    List<Order> findOrdersContainingProducts(@Param("productIds") List<Long> productIds);
}