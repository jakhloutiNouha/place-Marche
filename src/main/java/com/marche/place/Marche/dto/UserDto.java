package com.marche.place.Marche.dto;

import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String address;

    // Ajout de cette méthode pour la compatibilité
    public String getUsername() {
        return email; // Utiliser l'email comme username
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.imageUrl = user.getImageUrl();
        this.address = user.getAddress();
    }
}
