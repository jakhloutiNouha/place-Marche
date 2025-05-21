package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.PromotionDto;
import com.marche.place.Marche.entity.Product;
import com.marche.place.Marche.entity.Promotion;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.DiscountType;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.ProductRepository;
import com.marche.place.Marche.repository.PromotionRepository;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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

    public List<PromotionDto> getPromotionsByVendor(String username) {
        // Trouver l'utilisateur (vendeur) par son nom d'utilisateur (email)
        User vendor = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Récupérer tous les produits de ce vendeur en utilisant la méthode existante findByVendor
        List<Product> vendorProducts = productRepository.findByVendor(vendor);

        if (vendorProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // Récupérer toutes les promotions
        List<Promotion> allPromotions = promotionRepository.findAll();

        // Filtrer les promotions qui concernent les produits du vendeur
        return allPromotions.stream()
                .filter(promo ->
                        promo.getProducts().stream()
                                .anyMatch(product ->
                                        vendorProducts.stream()
                                                .anyMatch(vp -> vp.getId().equals(product.getId()))
                                )
                )
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