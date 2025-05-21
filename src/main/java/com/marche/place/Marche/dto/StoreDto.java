package com.marche.place.Marche.dto;

import com.marche.place.Marche.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {
    private Long id;
    private String title;
    private String name; // Pour compatibilité frontend
    private String description;
    private UserDto vendor;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String contact;
    private String logo;
    private String logoUrl;
    private String banner;
    private String bannerUrl;
    private String location;
    private Double rating;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeur personnalisé pour la conversion depuis l'entité
    public StoreDto(Store store) {
        this.id = store.getId();
        this.title = store.getTitle();
        this.name = store.getTitle(); // Pour compatibilité frontend
        this.description = store.getDescription();
        this.vendor = store.getVendor() != null ?
                new UserDto(store.getVendor()) :
                null;
        this.createdAt = store.getCreatedAt();
        this.updatedAt = store.getUpdatedAt();
    }

    // Getters personnalisés pour gérer la compatibilité frontend/backend
    public String getName() {
        return this.title;
    }

    public Long getVendorId() {
        return this.vendor != null ? this.vendor.getId() : null;
    }

    public String getVendorName() {
        return this.vendor != null ? this.vendor.getFullName() : null;
    }
}