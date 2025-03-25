package com.marche.place.Marche.dto;

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
    private UserRole role;
    private LocalDateTime createdAt; // Champ ajouté

    // Champs optionnels
    private String password;
    private String address;
}