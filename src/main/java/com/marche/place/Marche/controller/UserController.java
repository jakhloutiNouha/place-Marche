package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.UserDto;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.repository.UserRepository;
import com.marche.place.Marche.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Obtenir le profil de l'utilisateur connecté")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Non authentifié")
            );
        }

        // Log pour débogage
        System.out.println("Authentication name: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", ")));

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "Utilisateur non trouvé")
            );
        }

        User user = userOpt.get();
        UserDto userDto = userService.convertToDto(user);

        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Créer un utilisateur")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'Admin', 'ROLE_ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        try {
            UserDto createdUser = userService.createUser(userDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    @Operation(summary = "Obtenir un utilisateur par ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        // Log pour débogage
        System.out.println("getUserById called with id: " + id);

        if (authentication != null) {
            System.out.println("Authentication name: " + authentication.getName());
            System.out.println("Authorities: " + authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")));

            // Vérifier si l'utilisateur demande son propre profil
            Optional<User> userOpt = userRepository.findByEmail(authentication.getName());

            if (userOpt.isPresent() && userOpt.get().getId().equals(id)) {
                // L'utilisateur demande son propre profil, autoriser l'accès
                System.out.println("User requesting own profile, allowing access");
                return ResponseEntity.ok(userService.getUserById(id));
            }

            // Vérifier si l'utilisateur est administrateur
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth ->
                            auth.getAuthority().equals("ADMIN") ||
                                    auth.getAuthority().equals("Admin") ||
                                    auth.getAuthority().equals("ROLE_ADMIN")
                    );

            if (isAdmin) {
                System.out.println("Admin requesting profile, allowing access");
                return ResponseEntity.ok(userService.getUserById(id));
            }

            // Utilisateur non autorisé
            System.out.println("User not authorized to access this profile");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Accès refusé")
            );
        }

        // Non authentifié
        System.out.println("Unauthorized access attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "Non authentifié")
        );
    }

    @Operation(summary = "Mettre à jour un utilisateur")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Non authentifié")
            );
        }

        // Vérifier si l'utilisateur met à jour son propre profil
        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "Utilisateur non trouvé")
            );
        }

        User user = userOpt.get();
        boolean isOwnProfile = user.getId().equals(id);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth ->
                        auth.getAuthority().equals("ADMIN") ||
                                auth.getAuthority().equals("Admin") ||
                                auth.getAuthority().equals("ROLE_ADMIN")
                );

        if (!isOwnProfile && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Accès refusé")
            );
        }

        try {
            UserDto updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'Admin', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Rechercher des utilisateurs par rôle")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'Admin', 'ROLE_ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            List<User> users = userRepository.findByRole(userRole);
            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            List<UserDto> userDtos = users.stream()
                    .map(userService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rôle invalide : " + role));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Endpoint de débogage des utilisateurs accessible");
        response.put("timestamp", new java.util.Date());

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Liste des utilisateurs")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'Admin', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            // Debug logging
            if (authentication != null) {
                System.out.println("Current user: " + authentication.getName());
                System.out.println("Roles: " + authentication.getAuthorities());
            } else {
                System.out.println("No authentication found!");
            }

            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch users", "details", e.getMessage()));
        }
    }
}