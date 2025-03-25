package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.UserDto;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.exception.ConflictException;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;   // ✅ Correct
import java.util.stream.Collectors; // ✅ Ajoute cet import pour Collectors


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
        user.setRole(dto.getRole());

        return convertToDto(userRepository.save(user));
    }

    // Méthode de hachage temporaire
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
        if(dto.getPassword() != null) {
            user.setPassword(generateSimpleHash(dto.getPassword()));
        }

        return convertToDto(userRepository.save(user));
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}