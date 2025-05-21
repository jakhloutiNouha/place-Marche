package com.marche.place.Marche.service;

import com.marche.place.Marche.config.JwtUtil;
import com.marche.place.Marche.dto.AuthResponseDto;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthResponseDto login(String email, String password) {
        // Log pour le débogage
        System.out.println("AuthService: Tentative de connexion pour: " + email);

        // Rechercher l'utilisateur par email
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            System.out.println("AuthService: Utilisateur non trouvé avec cet email: " + email);
            return null;
        }

        // Vérifier le mot de passe avec le même hachage que l'inscription
        String hashedPassword = java.util.UUID.nameUUIDFromBytes(password.getBytes()).toString();

        // Logs de débogage
        System.out.println("=== DEBUG MOT DE PASSE ===");
        System.out.println("Mot de passe fourni: " + password);
        System.out.println("Hash calculé: " + hashedPassword);
        System.out.println("Hash stocké: " + user.getPassword());

        if (!user.getPassword().equals(hashedPassword)) {
            System.out.println("AuthService: Mot de passe incorrect pour: " + email);
            return null;
        }

        System.out.println("AuthService: Connexion réussie pour: " + email);

        // Charger les UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Générer un JWT valide - Passer user en second paramètre
        String jwt = jwtUtil.generateToken(userDetails, user);

        System.out.println("AuthService: JWT généré: " + jwt);

        // Construire et retourner la réponse
        return AuthResponseDto.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .id(user.getId())
                .build();
    }
}