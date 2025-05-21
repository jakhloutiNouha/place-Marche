package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.LoginDto;
import com.marche.place.Marche.dto.AuthResponseDto;
import com.marche.place.Marche.dto.UserDto;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.exception.ConflictException;
import com.marche.place.Marche.repository.UserRepository;
import com.marche.place.Marche.service.AuthService;
import com.marche.place.Marche.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints pour l'authentification")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(AuthService authService, UserService userService, UserRepository userRepository) {
        this.authService = authService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Inscription d'un nouvel utilisateur", description = "Crée un nouveau compte utilisateur")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
        try {
            // Log pour débugger
            System.out.println("Received registration request: " + userDto);

            // Vérifications des champs obligatoires
            if (userDto.getFullName() == null || userDto.getFullName().isEmpty()) {
                return ResponseEntity.badRequest().body("Le nom complet est obligatoire");
            }
            if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("L'email est obligatoire");
            }
            if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body("Le mot de passe est obligatoire");
            }
            if (userDto.getPhone() == null || userDto.getPhone().isEmpty()) {
                return ResponseEntity.badRequest().body("Le numéro de téléphone est obligatoire");
            }
            if (userDto.getAddress() == null || userDto.getAddress().isEmpty()) {
                return ResponseEntity.badRequest().body("L'adresse est obligatoire");
            }

            // Gestion du rôle selon vos besoins
            if (userDto.getRole() == null) {
                // Si aucun rôle n'est spécifié, définir par défaut comme Client
                userDto.setRole(UserRole.Client);
                System.out.println("Aucun rôle spécifié, définition par défaut à Client");
            } else if (userDto.getRole() == UserRole.Vendor) {
                // Si le rôle Vendor est spécifié, le conserver
                System.out.println("Rôle Vendor spécifié, conservation du rôle");
            } else {
                // Pour tout autre rôle, le conserver
                System.out.println("Rôle spécifié: " + userDto.getRole());
            }

            UserDto createdUser = userService.createUser(userDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (ConflictException e) {
            // Conflit - email déjà utilisé
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // Log l'erreur pour débugger
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur d'inscription: " + e.getMessage());
        }
    }

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("authenticated", false);
            response.put("message", "Non authentifié");
            return ResponseEntity.ok(response);
        }

        response.put("authenticated", true);
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug-token")
    public ResponseEntity<?> debugToken(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("authenticated", false);
            response.put("message", "Non authentifié");
            return ResponseEntity.ok(response);
        }

        response.put("authenticated", true);
        response.put("principal", authentication.getPrincipal());
        response.put("name", authentication.getName());

        // Vérifier les autorités
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        response.put("authorities", authorities);

        // Vérifier si l'utilisateur est admin
        boolean isAdmin = authorities.stream()
                .anyMatch(auth ->
                        auth.equals("ADMIN") ||
                                auth.equals("Admin") ||
                                auth.equals("ROLE_ADMIN")
                );

        response.put("isAdmin", isAdmin);

        // Obtenir les informations de l'utilisateur depuis la base de données
        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            response.put("userInfo", userInfo);
        } else {
            response.put("userInfo", "User not found in database");
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur avec email et mot de passe")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            AuthResponseDto response = authService.login(loginDto.getEmail(), loginDto.getPassword());
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur: " + e.getMessage());
        }
    }
}