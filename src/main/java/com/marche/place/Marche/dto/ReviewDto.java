package com.marche.place.Marche.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private String content;
    private String title;
    private int rating;
    private Long storeId;  // Remplacer productId par storeId
    private String storeName;  // Ajouter le nom de la boutique
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;

    // Constructeurs
    public ReviewDto() {
    }

    public ReviewDto(Long id, String content, String title, int rating, Long storeId, String storeName,
                     Long userId, String userName, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.title = title;
        this.rating = rating;
        this.storeId = storeId;
        this.storeName = storeName;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}