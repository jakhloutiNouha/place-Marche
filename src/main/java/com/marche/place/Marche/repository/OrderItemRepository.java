package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Trouver tous les items d'une commande
    List<OrderItem> findByOrderId(Long orderId);

    // Trouver tous les items contenant un produit spécifique
    List<OrderItem> findByProductId(Long productId);

    // Requête pour trouver les produits les plus vendus
    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi GROUP BY oi.product.id ORDER BY totalQuantity DESC")
    List<Object[]> findMostSoldProducts();

    // Requête pour trouver le nombre total de ventes pour un produit
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Integer getTotalSalesForProduct(@Param("productId") Long productId);
}