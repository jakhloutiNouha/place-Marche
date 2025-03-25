// PromotionRepository.java
package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Promotion;
import com.marche.place.Marche.enums.DiscountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Page<Promotion> findByStartDateBeforeAndEndDateAfter(
            LocalDateTime date,
            LocalDateTime sameDate,
            Pageable pageable
    );
    Page<Promotion> findByDiscountType(DiscountType discountType, Pageable pageable);
}