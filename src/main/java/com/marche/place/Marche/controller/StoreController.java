package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.ProductDto;
import com.marche.place.Marche.dto.StoreDto;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.service.ProductService;
import com.marche.place.Marche.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store Management", description = "Endpoints pour gérer les magasins")
public class StoreController {

    private final StoreService storeService;
    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Créer un magasin", description = "Crée un nouveau magasin pour le vendeur connecté.")
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto storeDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        StoreDto createdStore = storeService.createStoreForVendor(storeDto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);
    }

    @GetMapping("/{storeId}/products")
    @Operation(summary = "Récupérer les produits d'un magasin", description = "Retourne tous les produits associés à un magasin spécifique.")
    public ResponseEntity<?> getStoreProducts(
            @PathVariable Long storeId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {

        try {
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy)
            );

            List<ProductDto> products = productService.getProductsByStoreId(storeId, pageable);
            return ResponseEntity.ok(products);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Magasin non trouvé avec l'ID: " + storeId);
        } catch (Exception e) {
            // Log détaillé de l'erreur
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur est survenue lors de la récupération des produits: " + e.getMessage());
        }
    }

    @GetMapping("/vendor")
    @Operation(summary = "Récupérer les magasins du vendeur connecté", description = "Retourne tous les magasins du vendeur actuellement connecté.")
    public ResponseEntity<List<StoreDto>> getVendorStores() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("Email du vendeur authentifié: " + email);
        List<StoreDto> stores = storeService.getStoresByVendorEmail(email);
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/vendor/{vendorId}")
    @Operation(summary = "Récupérer les magasins d'un vendeur par ID", description = "Retourne tous les magasins associés à un vendeur spécifique.")
    public ResponseEntity<Page<StoreDto>> getStoresByVendor(
            @PathVariable Long vendorId,
            Pageable pageable) {
        Page<StoreDto> stores = storeService.getStoresByVendor(vendorId, pageable);
        return ResponseEntity.ok(stores);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les magasins", description = "Retourne tous les magasins paginés.")
    public ResponseEntity<Page<StoreDto>> getAllStores(Pageable pageable) {
        return ResponseEntity.ok(storeService.getAllStores(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un magasin par ID", description = "Retourne les détails d'un magasin spécifique.")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @GetMapping("/{id}/product-count")
    @Operation(summary = "Compter les produits d'un magasin", description = "Retourne le nombre de produits dans un magasin spécifique.")
    public ResponseEntity<Integer> getStoreProductCount(@PathVariable Long id) {
        int count = storeService.getStoreProductCount(id);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un magasin", description = "Met à jour les informations d'un magasin spécifique.")
    public ResponseEntity<StoreDto> updateStore(
            @PathVariable Long id,
            @RequestBody StoreDto storeDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(storeService.updateStoreForVendor(id, storeDto, email));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un magasin", description = "Supprime un magasin spécifique.")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        storeService.deleteStoreForVendor(id, email);
        return ResponseEntity.noContent().build();
    }
}