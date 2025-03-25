package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.StoreDto;
import com.marche.place.Marche.dto.UserDto;
import com.marche.place.Marche.entity.Store;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.StoreRepository;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public StoreDto createStore(StoreDto storeDto) {
        User vendor = userRepository.findById(storeDto.getVendor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé"));

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
    private LocalDateTime convertJodaToJava(org.joda.time.LocalDateTime jodaTime) {
        if (jodaTime == null) return null; // ✅ Évite NullPointerException

        return LocalDateTime.of(
                jodaTime.getYear(),
                jodaTime.getMonthOfYear(),
                jodaTime.getDayOfMonth(),
                jodaTime.getHourOfDay(),
                jodaTime.getMinuteOfHour(),
                jodaTime.getSecondOfMinute()
        );
    }

    private StoreDto convertToDto(Store store) {
        User vendor = store.getVendor();

        return StoreDto.builder()
                .id(store.getId())
                .title(store.getTitle())
                .description(store.getDescription())
                .vendor(
                        UserDto.builder()
                                .id(vendor.getId())
                                .fullName(vendor.getFullName())
                                .email(vendor.getEmail())
                                .phone(vendor.getPhone())
                                .role(vendor.getRole())
                                .createdAt(convertJodaToJava(vendor.getCreatedAt()))  // ✅ Correction
                                .build()
                )
                .createdAt(store.getCreatedAt())
                .build();
    }

    public Page<StoreDto> getAllStores(Pageable pageable) {
        return storeRepository.findAll(pageable).map(this::convertToDto);
    }

    public StoreDto getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));
        return convertToDto(store);
    }

    public StoreDto updateStore(Long id, StoreDto storeDto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));

        store.setTitle(storeDto.getTitle());
        store.setDescription(storeDto.getDescription());

        return convertToDto(storeRepository.save(store));
    }

    public void deleteStore(Long id) {
        if (!storeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Magasin non trouvé");
        }
        storeRepository.deleteById(id);
    }
}
