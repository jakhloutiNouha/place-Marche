package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.ReviewDto;
import com.marche.place.Marche.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Store Review Management", description = "Endpoints pour la gestion des avis sur les boutiques")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Obtenir tous les avis", description = "Retourne la liste complète de tous les avis sur les boutiques")
    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<ReviewDto> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Soumettre un avis", description = "Permet à un utilisateur connecté d'ajouter un avis pour une boutique")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'VENDOR', 'CLIENT', 'Client', 'Admin', 'Vendor')")
    public ResponseEntity<ReviewDto> createReview(
            @RequestBody ReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ReviewDto createdReview = reviewService.createReview(reviewDto, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Obtenir les avis d'un utilisateur", description = "Retourne tous les avis soumis par un utilisateur spécifique")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewDto> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Obtenir la note moyenne", description = "Calcule la note moyenne pour une boutique")
    @GetMapping("/store/{storeId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long storeId) {
        Double averageRating = reviewService.getAverageRatingForStore(storeId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }

    @Operation(summary = "Obtenir les statistiques de notation", description = "Retourne la distribution des notes pour une boutique")
    @GetMapping("/store/{storeId}/stats")
    public ResponseEntity<Map<Integer, Long>> getRatingStats(@PathVariable Long storeId) {
        Map<Integer, Long> stats = reviewService.getRatingStatsForStore(storeId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Mettre à jour un avis", description = "Permet à un utilisateur de modifier son avis")
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'VENDOR', 'CLIENT', 'Client', 'Admin', 'Vendor')")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReviewDto updatedReview = reviewService.updateReview(reviewId, reviewDto, userDetails.getUsername());
        return ResponseEntity.ok(updatedReview);
    }

    @Operation(summary = "Supprimer un avis", description = "Permet à un utilisateur de supprimer son avis ou à un admin de supprimer n'importe quel avis")
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'VENDOR', 'CLIENT', 'Client', 'Admin', 'Vendor')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir les avis d'une boutique", description = "Retourne tous les avis pour une boutique spécifique")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByStore(@PathVariable Long storeId) {
        List<ReviewDto> reviews = reviewService.getReviewsByStoreId(storeId);
        return ResponseEntity.ok(reviews);
    }
}