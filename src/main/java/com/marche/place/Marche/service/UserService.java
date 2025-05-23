package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.UserDto;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.exception.ConflictException;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserDto createUser(UserDto dto) {
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email déjà utilisé");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(generateSimpleHash(dto.getPassword()));
        user.setPhone(dto.getPhone());
        // Si aucun rôle n'est spécifié, définir Client par défaut
        user.setRole(dto.getRole() != null ? dto.getRole() : UserRole.Client);
        user.setAddress(dto.getAddress());

        return convertToDto(userRepository.save(user));
    }

    private String generateSimpleHash(String password) {
        return UUID.nameUUIDFromBytes(password.getBytes()).toString();
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        if(dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(generateSimpleHash(dto.getPassword()));
        }

        return convertToDto(userRepository.save(user));
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé");
        }
        userRepository.deleteById(id);
        return true;
    }

    // Rendre cette méthode publique pour utilisation dans les contrôleurs
    public UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .imageUrl(user.getImageUrl())
                .build();
    }
}