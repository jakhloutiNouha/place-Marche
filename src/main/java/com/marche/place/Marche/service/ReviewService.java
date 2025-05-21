package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.ReviewDto;
import com.marche.place.Marche.entity.Review;
import com.marche.place.Marche.entity.Store;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.repository.ReviewRepository;
import com.marche.place.Marche.repository.StoreRepository;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(
            ReviewRepository reviewRepository,
            StoreRepository storeRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    // Convertir une entité Review en ReviewDto
    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setTitle(review.getTitle() != null ? review.getTitle() :
                (review.getContent().length() > 30 ?
                        review.getContent().substring(0, 30) + "..." :
                        review.getContent()));
        dto.setRating(review.getRating());
        dto.setStoreId(review.getStore().getId());
        dto.setStoreName(review.getStore().getTitle());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getFullName() != null ?
                review.getUser().getFullName() :
                review.getUser().getEmail());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    // Créer un nouvel avis
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Store store = storeRepository.findById(reviewDto.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée"));

        // Vérifier si l'utilisateur a déjà laissé un avis pour cette boutique
        if (reviewRepository.existsByUserIdAndStoreId(user.getId(), store.getId())) {
            throw new IllegalStateException("Vous avez déjà laissé un avis pour cette boutique");
        }

        Review review = new Review();
        review.setContent(reviewDto.getContent());
        review.setTitle(reviewDto.getTitle());
        review.setRating(reviewDto.getRating());
        review.setStore(store);
        review.setUser(user);

        Review savedReview = reviewRepository.save(review);
        return convertToDto(savedReview);
    }

    // Obtenir tous les avis pour une boutique
    public List<ReviewDto> getReviewsByStoreId(Long storeId) {
        List<Review> reviews = reviewRepository.findByStoreId(storeId);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Obtenir tous les avis par utilisateur
    public List<ReviewDto> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Obtenir la note moyenne pour une boutique
    public Double getAverageRatingForStore(Long storeId) {
        return reviewRepository.calculateAverageRatingForStore(storeId);
    }

    // Obtenir les statistiques de notation pour une boutique
    public Map<Integer, Long> getRatingStatsForStore(Long storeId) {
        List<Object[]> ratingStats = reviewRepository.countReviewsByRatingForStore(storeId);
        Map<Integer, Long> stats = new HashMap<>();

        // Initialiser toutes les notes de 1 à 5 avec un compte de 0
        for (int i = 1; i <= 5; i++) {
            stats.put(i, 0L);
        }

        // Remplir avec les valeurs réelles
        for (Object[] stat : ratingStats) {
            Integer rating = (Integer) stat[0];
            Long count = (Long) stat[1];
            stats.put(rating, count);
        }

        return stats;
    }

    // Mettre à jour un avis
    @Transactional
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Avis non trouvé"));

        // Vérifier que l'utilisateur est bien le propriétaire de l'avis
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à modifier cet avis");
        }

        review.setContent(reviewDto.getContent());
        review.setTitle(reviewDto.getTitle());
        review.setRating(reviewDto.getRating());

        Review updatedReview = reviewRepository.save(review);
        return convertToDto(updatedReview);
    }

    // Supprimer un avis
    @Transactional
    public void deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Avis non trouvé"));

        // Vérifier que l'utilisateur est bien le propriétaire de l'avis ou un admin
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (!review.getUser().getId().equals(user.getId()) && !user.getRole().equals("Admin")) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à supprimer cet avis");
        }

        reviewRepository.delete(review);
    }

    // Obtenir tous les avis
    public List<ReviewDto> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}