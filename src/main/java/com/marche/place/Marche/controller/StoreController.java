package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.StoreDto;
import com.marche.place.Marche.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store Management", description = "Endpoints pour gérer les magasins")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "Créer un magasin", description = "Ajoute un nouveau magasin à la base de données.")
    @PostMapping
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto storeDto) {
        StoreDto createdStore = storeService.createStore(storeDto);
        return ResponseEntity.ok(createdStore);
    }

    @Operation(summary = "Lister tous les magasins", description = "Retourne une liste paginée des magasins.")
    @GetMapping
    public ResponseEntity<Page<StoreDto>> getAllStores(Pageable pageable) {
        Page<StoreDto> stores = storeService.getAllStores(pageable);
        return ResponseEntity.ok(stores);
    }

    @Operation(summary = "Obtenir un magasin", description = "Retourne un magasin spécifique par ID.")
    @GetMapping("/{id}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable Long id) {
        StoreDto store = storeService.getStoreById(id);
        return ResponseEntity.ok(store);
    }

    @Operation(summary = "Lister les magasins d'un vendeur", description = "Retourne une liste paginée des magasins d'un vendeur spécifique.")
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<Page<StoreDto>> getStoresByVendor(@PathVariable Long vendorId, Pageable pageable) {
        Page<StoreDto> stores = storeService.getStoresByVendor(vendorId, pageable);
        return ResponseEntity.ok(stores);
    }

    @Operation(summary = "Mettre à jour un magasin", description = "Modifie un magasin existant en utilisant son ID.")
    @PutMapping("/{id}")
    public ResponseEntity<StoreDto> updateStore(@PathVariable Long id, @RequestBody StoreDto storeDto) {
        StoreDto updatedStore = storeService.updateStore(id, storeDto);
        return ResponseEntity.ok(updatedStore);
    }

    @Operation(summary = "Supprimer un magasin", description = "Supprime un magasin spécifique en utilisant son ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}
