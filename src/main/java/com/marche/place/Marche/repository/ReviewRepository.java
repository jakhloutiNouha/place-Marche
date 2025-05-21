package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Trouver tous les avis pour une boutique spécifique
    List<Review> findByStoreId(Long storeId);

    // Trouver tous les avis par un utilisateur spécifique
    List<Review> findByUserId(Long userId);

    // Trouver tous les avis pour une boutique avec un rating minimum
    List<Review> findByStoreIdAndRatingGreaterThanEqual(Long storeId, int minRating);

    // Calculer la note moyenne pour une boutique
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.id = :storeId")
    Double calculateAverageRatingForStore(@Param("storeId") Long storeId);

    // Compter le nombre d'avis par note pour une boutique
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.store.id = :storeId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> countReviewsByRatingForStore(@Param("storeId") Long storeId);

    // Supprimer tous les avis d'une boutique
    void deleteByStoreId(Long storeId);

    // Supprimer tous les avis d'un utilisateur
    void deleteByUserId(Long userId);

    // Vérifier si un utilisateur a déjà laissé un avis pour cette boutique
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);
}