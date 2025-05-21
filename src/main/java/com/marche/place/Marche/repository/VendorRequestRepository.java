package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.VendorRequest;
import com.marche.place.Marche.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendorRequestRepository extends JpaRepository<VendorRequest, Long> {
    List<VendorRequest> findByStatus(RequestStatus status);
    boolean existsByEmail(String email);
}