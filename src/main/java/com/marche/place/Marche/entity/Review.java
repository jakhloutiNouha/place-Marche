package com.marche.place.Marche.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000, nullable = false)
    private String content;

    @Column(length = 100)
    private String title;

    @Column(nullable = false)
    private int rating; // 1 to 5 stars rating

    // Remplacer la relation avec Product par une relation avec Store
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

    // equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return rating == review.rating &&
                Objects.equals(id, review.id) &&
                Objects.equals(content, review.content) &&
                Objects.equals(title, review.title) &&
                Objects.equals(store, review.store) &&
                Objects.equals(user, review.user);
    }

    // hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(id, content, title, rating, store, user);
    }

    // canEqual method
    public boolean canEqual(Object other) {
        return other instanceof Review;
    }
}