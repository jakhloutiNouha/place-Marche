// AuthController.java
package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.LoginDto;
import com.marche.place.Marche.dto.AuthResponseDto;
import com.marche.place.Marche.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints pour l'authentification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur avec email et mot de passe")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        System.out.println("====== AuthController: Requête de login reçue ======");
        System.out.println("Email: " + loginDto.getEmail());
        System.out.println("Password reçu (longueur): " + (loginDto.getPassword() != null ? loginDto.getPassword().length() : "null"));

        try {
            AuthResponseDto response = authService.login(loginDto.getEmail(), loginDto.getPassword());

            if (response != null) {
                System.out.println("Login réussi - Retour token: " + response.getToken());
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Login échoué - Retour 401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
            }
        } catch (Exception e) {
            System.out.println("ERREUR EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur: " + e.getMessage() + "\n" + e.getClass().getName());
        }
    }
}