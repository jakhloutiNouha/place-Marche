package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByVendorId(Long vendorId);

    Page<Store> findByVendorId(Long vendorId, Pageable pageable);

    Page<Store> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}