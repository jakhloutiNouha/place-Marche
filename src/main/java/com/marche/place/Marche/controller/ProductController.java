package com.marche.place.Marche.controller;

import com.marche.place.Marche.dto.ProductDto;
import com.marche.place.Marche.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints pour la gestion des produits")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Lister tous les produits", description = "Retourne la liste des produits disponibles.")
    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @Operation(summary = "Obtenir un produit", description = "Retourne un produit spécifique par ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Créer un produit", description = "Ajoute un nouveau produit à la base de données.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @Operation(summary = "Mettre à jour un produit", description = "Modifie un produit existant.")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    @Operation(summary = "Supprimer un produit", description = "Supprime un produit spécifique.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
