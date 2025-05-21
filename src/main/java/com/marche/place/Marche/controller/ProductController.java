package com.marche.place.Marche.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.marche.place.Marche.dto.ProductDto;
import com.marche.place.Marche.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import com.marche.place.Marche.entity.Product;
import com.marche.place.Marche.entity.Product;
import com.marche.place.Marche.service.ProductService;
import com.marche.place.Marche.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints pour la gestion des produits")
public class ProductController {


    @Autowired
    private ProductService productService; // Assurez-vous que vous n'avez pas de deuxième déclaration

    @GetMapping("/vendor/{vendorId}")
    public List<ProductDto> getProductsByVendor(@PathVariable Long vendorId) {
        List<Product> products = productService.getProductsByVendor(vendorId);
        return products.stream()
                .map(product -> productService.mapToDto(product))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Lister tous les produits avec filtres", description = "Retourne la liste des produits disponibles avec filtrage et pagination.")
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        List<ProductDto> products = productService.getAllProducts(
                page, pageSize, sortBy, sortOrder, categoryId, minPrice, maxPrice
        );
        return ResponseEntity.ok(products);
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

    @Autowired
    private ProductRepository productRepository; // Ajouter cette ligne

    @GetMapping("/products/all")
    public List<Product> getAllProducts() {
        return productRepository.findAll(); // Utiliser le repository ici
    }



    @GetMapping("/best-selling")
    public List<ProductDto> getBestSellingProducts(
            @RequestParam(defaultValue = "4") int limit
    ) {
        return productService.getBestSellingProducts(limit);
    }

    @GetMapping("/new-arrivals")
    public List<ProductDto> getNewArrivals(
            @RequestParam(defaultValue = "4") int limit
    ) {
        return productService.getNewArrivals(limit);
    }
    @Operation(summary = "Créer un produit en tant que vendeur", description = "Ajoute un nouveau produit associé au vendeur authentifié.")
    @PostMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductDto> createProductAsVendor(
            @RequestBody ProductDto productDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = userDetails.getUsername();
        ProductDto createdProduct = productService.createProductForVendor(productDto, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping("/featured")
    public List<ProductDto> getFeaturedProducts(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return productService.getFeaturedProducts(limit);
    }
    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<Product>> getProductsForVendor(
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("UserDetails dans getProductsForVendor: " + userDetails);

        if (userDetails == null) {
            System.out.println("ERREUR: UserDetails est null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ArrayList<>());
        }

        String username = userDetails.getUsername();
        System.out.println("Username du vendeur: " + username);

        List<Product> products = productService.getProductsByVendorUsername(username);
        System.out.println("Nombre de produits trouvés: " + products.size());

        return ResponseEntity.ok(products);
    }
    @GetMapping("/with-promotions")
    public ResponseEntity<List<ProductDto>> getProductsWithPromotions() {
        List<ProductDto> productsWithPromotions = productService.getProductsWithPromotions();
        return ResponseEntity.ok(productsWithPromotions);
    }

    /**
     * Récupère tous les produits sans promotion
     */
    @GetMapping("/without-promotions")
    public ResponseEntity<List<ProductDto>> getProductsWithoutPromotions() {
        List<ProductDto> productsWithoutPromotions = productService.getProductsWithoutPromotions();
        return ResponseEntity.ok(productsWithoutPromotions);
    }

    @Operation(summary = "Supprimer un produit", description = "Supprime un produit spécifique.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
