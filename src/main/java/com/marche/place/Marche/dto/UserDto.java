package com.marche.place.Marche.dto;

import com.marche.place.Marche.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;  // Assurez-vous que c'est java.time

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
    private LocalDateTime createdAt;  // Doit être java.time.LocalDateTime
    private String imageUrl;

    // Champs optionnels
    private String password;
    private String address;
}