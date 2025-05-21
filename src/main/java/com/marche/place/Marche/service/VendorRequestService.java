package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.*;
import com.marche.place.Marche.entity.*;
import com.marche.place.Marche.enums.*;
import com.marche.place.Marche.exception.*;
import com.marche.place.Marche.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorRequestService {

    private final VendorRequestRepository vendorRequestRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public VendorRequestDto submitRequest(VendorRequestDto dto) {
        if (vendorRequestRepository.existsByEmail(dto.getEmail()) ||
                userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email déjà utilisé");
        }

        VendorRequest request = new VendorRequest();
        request.setFullName(dto.getFullName());
        request.setEmail(dto.getEmail());
        request.setPhone(dto.getPhone());
        request.setAddress(dto.getAddress());
        request.setStoreName(dto.getStoreName());
        request.setStoreDescription(dto.getStoreDescription());
        request.setStoreAddress(dto.getStoreAddress());
        request.setStatus(RequestStatus.PENDING);

        return convertToDto(vendorRequestRepository.save(request));
    }

    public VendorRequestDto approveRequest(Long requestId) {
        VendorRequest request = vendorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        // Créer l'utilisateur
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setPassword(generateTemporaryPassword());
        user.setRole(UserRole.Vendor);
        user = userRepository.save(user);

        // Créer le store
        Store store = new Store();
        store.setTitle(request.getStoreName());  // Utiliser setTitle au lieu de setName

        // Combiner description et adresse du magasin
        String combinedDescription = request.getStoreDescription();
        if (request.getStoreAddress() != null && !request.getStoreAddress().isEmpty()) {
            combinedDescription += "\n\nAdresse: " + request.getStoreAddress();
        }
        store.setDescription(combinedDescription);

        store.setVendor(user);
        storeRepository.save(store);

        // Mettre à jour la demande
        request.setStatus(RequestStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());

        return convertToDto(vendorRequestRepository.save(request));
    }

    public VendorRequestDto rejectRequest(Long requestId, String reason) {
        VendorRequest request = vendorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setProcessedAt(LocalDateTime.now());

        return convertToDto(vendorRequestRepository.save(request));
    }

    public List<VendorRequestDto> getAllRequests() {
        return vendorRequestRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<VendorRequestDto> getPendingRequests() {
        return vendorRequestRepository.findByStatus(RequestStatus.PENDING).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private VendorRequestDto convertToDto(VendorRequest request) {
        return VendorRequestDto.builder()
                .id(request.getId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .storeName(request.getStoreName())
                .storeDescription(request.getStoreDescription())
                .storeAddress(request.getStoreAddress())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .processedAt(request.getProcessedAt())
                .rejectionReason(request.getRejectionReason())
                .build();
    }
}