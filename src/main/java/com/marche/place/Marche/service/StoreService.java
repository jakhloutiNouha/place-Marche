package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.ReviewDto;
import com.marche.place.Marche.dto.StoreDto;
import com.marche.place.Marche.dto.UserDto;
import com.marche.place.Marche.entity.Review;
import com.marche.place.Marche.entity.Store;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.exception.UnauthorizedException;
import com.marche.place.Marche.repository.ProductRepository;
import com.marche.place.Marche.repository.StoreRepository;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setTitle(review.getTitle() != null ? review.getTitle() :
                (review.getContent().length() > 30 ?
                        review.getContent().substring(0, 30) + "..." :
                        review.getContent()));
        dto.setRating(review.getRating());
        dto.setStoreId(review.getStore().getId());
        dto.setStoreName(review.getStore().getTitle());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getFullName() != null ?
                review.getUser().getFullName() :
                review.getUser().getEmail());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
    public StoreDto createStoreForVendor(StoreDto storeDto, String vendorEmail) {
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'email: " + vendorEmail));

        if (!vendor.getRole().equals(UserRole.Vendor)) {
            throw new UnauthorizedException("Seuls les vendeurs peuvent créer un magasin");
        }

        Store store = new Store();
        store.setTitle(storeDto.getTitle());
        store.setDescription(storeDto.getDescription());
        store.setVendor(vendor);

        return convertToDto(storeRepository.save(store));
    }

    public Page<StoreDto> getStoresByVendor(Long vendorId, Pageable pageable) {
        return storeRepository.findByVendorId(vendorId, pageable)
                .map(this::convertToDto);
    }

    public List<StoreDto> getStoresByVendorEmail(String email) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'email: " + email));

        if (!vendor.getRole().equals(UserRole.Vendor)) {
            throw new UnauthorizedException("Seuls les vendeurs peuvent accéder à cette ressource");
        }

        return storeRepository.findByVendorId(vendor.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<StoreDto> getAllStores(Pageable pageable) {
        return storeRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public StoreDto getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé avec l'ID: " + id));
        return convertToDto(store);
    }

    public StoreDto updateStore(Long id, StoreDto storeDto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));

        store.setTitle(storeDto.getTitle());
        store.setDescription(storeDto.getDescription());

        return convertToDto(storeRepository.save(store));
    }

    public StoreDto updateStoreForVendor(Long id, StoreDto storeDto, String vendorEmail) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé avec l'ID: " + id));

        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'email: " + vendorEmail));

        if (!store.getVendor().getId().equals(vendor.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce magasin");
        }

        // Mettre à jour les champs du magasin
        if (storeDto.getTitle() != null) store.setTitle(storeDto.getTitle());
        if (storeDto.getDescription() != null) store.setDescription(storeDto.getDescription());

        // Mise à jour de la date
        store.setUpdatedAt(LocalDateTime.now());

        return convertToDto(storeRepository.save(store));
    }

    public void deleteStore(Long id) {
        if (!storeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Magasin non trouvé");
        }
        storeRepository.deleteById(id);
    }

    public void deleteStoreForVendor(Long id, String vendorEmail) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé avec l'ID: " + id));

        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'email: " + vendorEmail));

        if (!store.getVendor().getId().equals(vendor.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer ce magasin");
        }

        storeRepository.delete(store);
    }

    public int getStoreProductCount(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Magasin non trouvé avec l'ID: " + storeId);
        }

        return productRepository.countByStoreId(storeId);
    }

    private StoreDto convertToDto(Store store) {
        User vendor = store.getVendor();

        StoreDto dto = StoreDto.builder()
                .id(store.getId())
                .title(store.getTitle())
                .description(store.getDescription())
                .vendor(UserDto.builder()
                        .id(vendor.getId())
                        .fullName(vendor.getFullName())
                        .email(vendor.getEmail())
                        .phone(vendor.getPhone())
                        .role(vendor.getRole())
                        .createdAt(vendor.getCreatedAt())
                        .build())
                .createdAt(store.getCreatedAt())
                .build();

        return dto;
    }
}