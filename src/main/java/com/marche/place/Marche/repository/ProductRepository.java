package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Product;
import com.marche.place.Marche.entity.Store;
import com.marche.place.Marche.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByStore(Store store);
    List<Product> findByVendor(User vendor);

    // Ajout de la méthode manquante
    @Query("SELECT p FROM Product p WHERE p.vendor.id = :vendorId")
    List<Product> findByVendorId(@Param("vendorId") Long vendorId);

    // Méthodes pour récupérer les produits par storeId
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId")
    List<Product> findByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId")
    Page<Product> findByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    // Méthode pour compter les produits d'un magasin
    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.id = :storeId")
    Integer countByStoreId(@Param("storeId") Long storeId);
}