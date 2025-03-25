package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.PromotionDto;
import com.marche.place.Marche.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion Management", description = "Endpoints pour la gestion des promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @Operation(summary = "Créer une promotion", description = "Ajoute une nouvelle promotion.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionDto createPromotion(@RequestBody PromotionDto promotionDto) {
        return promotionService.createPromotion(promotionDto);
    }

    @Operation(summary = "Lister toutes les promotions", description = "Retourne la liste des promotions actives.")
    @GetMapping
    public ResponseEntity<List<PromotionDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @Operation(summary = "Obtenir une promotion", description = "Retourne une promotion spécifique par ID.")
    @GetMapping("/{id}")
    public ResponseEntity<PromotionDto> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @Operation(summary = "Mettre à jour une promotion", description = "Modifie une promotion existante.")
    @PutMapping("/{id}")
    public ResponseEntity<PromotionDto> updatePromotion(@PathVariable Long id, @RequestBody PromotionDto promotionDto) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, promotionDto));
    }

    @Operation(summary = "Supprimer une promotion", description = "Supprime une promotion spécifique.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
    }
}
