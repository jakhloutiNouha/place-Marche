package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Store;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;  // Ajout de l'import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByStoresVendorId(Long vendorId, Pageable pageable);
    Page<Store> findByVendorId(Long vendorId, Pageable pageable);

    List<User> findByRole(UserRole role); // Correction de la signature
}
