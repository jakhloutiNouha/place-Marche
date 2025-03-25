package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.PromotionDto;
import com.marche.place.Marche.entity.Product;
import com.marche.place.Marche.entity.Promotion;
import com.marche.place.Marche.enums.DiscountType;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.ProductRepository;
import com.marche.place.Marche.repository.PromotionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    public PromotionDto createPromotion(PromotionDto dto) {
        Promotion promotion = new Promotion();
        promotion.setCode("PROMO-" + System.currentTimeMillis());
        promotion.setDiscountValue(BigDecimal.valueOf(dto.getDiscountPercentage()));
        promotion.setDiscountType(DiscountType.PERCENTAGE);
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());

        // Vérifier que le produit existe
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        promotion.getProducts().add(product);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return mapToDto(savedPromotion);
    }

    public List<PromotionDto> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PromotionDto getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        return mapToDto(promotion);
    }

    public PromotionDto updatePromotion(Long id, PromotionDto dto) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        if (dto.getDiscountPercentage() > 0) {
            promotion.setDiscountValue(BigDecimal.valueOf(dto.getDiscountPercentage()));
        }
        if (dto.getStartDate() != null) {
            promotion.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            promotion.setEndDate(dto.getEndDate());
        }

        Promotion updatedPromotion = promotionRepository.save(promotion);
        return mapToDto(updatedPromotion);
    }

    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        promotionRepository.delete(promotion);
    }

    private PromotionDto mapToDto(Promotion promotion) {
        return new PromotionDto(
                promotion.getId(),
                promotion.getDiscountValue().doubleValue(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getProducts().isEmpty() ? null : promotion.getProducts().get(0).getId()
        );
    }
}
