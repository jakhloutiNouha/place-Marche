package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.CategoryDto;
import com.marche.place.Marche.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "Endpoints pour gérer les catégories de produits")
public class CategoryController {

    private final CategoryService categoryService;

    // Injection du service via le constructeur
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Lister les catégories du vendeur connecté")
    @GetMapping("/vendor")
    public ResponseEntity<List<CategoryDto>> getVendorCategories() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("Email extrait du contexte de sécurité : " + email);
        System.out.println("Authorities : " + auth.getAuthorities());
        List<CategoryDto> categories = categoryService.getVendorCategories(email);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Créer une catégorie pour le vendeur connecté")
    @PostMapping("/vendor")
    public ResponseEntity<CategoryDto> createVendorCategory(@Valid @RequestBody CategoryDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        CategoryDto createdCategory = categoryService.createVendorCategory(dto, email);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @Operation(summary = "Créer une catégorie", description = "Ajoute une nouvelle catégorie à la base de données.")
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        return new ResponseEntity<>(categoryService.createCategory(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Lister toutes les catégories", description = "Retourne la liste de toutes les catégories disponibles.")
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Obtenir une catégorie par ID", description = "Retourne les détails d'une catégorie spécifique en utilisant son ID.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Mettre à jour une catégorie", description = "Modifie une catégorie existante en utilisant son ID.")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @Operation(summary = "Supprimer une catégorie", description = "Supprime une catégorie spécifique en utilisant son ID.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}