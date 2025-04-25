// AuthService.java
package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.AuthResponseDto;
import com.marche.place.Marche.dto.UserDto;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public AuthResponseDto login(String email, String password) {
        try {
            System.out.println("====== Début tentative de login ======");
            System.out.println("Email reçu: " + email);
            System.out.println("Password reçu: " + password);

            if (email == null || password == null) {
                System.out.println("Email ou password est null");
                return null;
            }

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                System.out.println("ERREUR: Aucun utilisateur trouvé avec l'email: " + email);
                return null;
            }

            System.out.println("Utilisateur trouvé: " + user.getFullName());
            System.out.println("Role: " + user.getRole());
            System.out.println("Mot de passe en base: " + user.getPassword());

            // Comparaison directe pour test
            if (user.getPassword().equals(password)) {
                System.out.println("Authentification réussie avec comparaison directe");
                String token = UUID.randomUUID().toString();

                AuthResponseDto response = AuthResponseDto.builder()
                        .token(token)
                        .role(user.getRole() != null ? user.getRole().toString() : "")
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .build();

                System.out.println("Réponse créée: " + response);
                return response;
            }

            System.out.println("ERREUR: Mot de passe incorrect");
            return null;

        } catch (Exception e) {
            System.out.println("ERREUR EXCEPTION dans AuthService: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    // AuthService.java

}