package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.ProductDto;
import com.marche.place.Marche.entity.*;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public List<Product> getProductsByVendor(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id " + vendorId));
        return productRepository.findByVendor(vendor);
    }

    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public List<ProductDto> getAllProducts(Integer page, Integer pageSize, String sortBy, String sortOrder,
                                           Long categoryId, Double minPrice, Double maxPrice) {
        Specification<Product> spec = Specification.where(null);

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "id");

        Pageable pageable = PageRequest.of(
                page != null && page > 0 ? page - 1 : 0,
                pageSize != null ? pageSize : 12,
                sort);

        return productRepository.findAll(spec, pageable)
                .map(this::mapToDto)
                .getContent();
    }

    public List<ProductDto> getBestSellingProducts(int limit) {
        return productRepository.findAll(
                        PageRequest.of(0, limit, Sort.by("rating").descending()))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getNewArrivals(int limit) {
        return productRepository.findAll(
                        PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getFeaturedProducts(int limit) {
        return productRepository.findAll(PageRequest.of(0, limit))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    public List<ProductDto> getProductsByStoreId(Long storeId, Pageable pageable) {
        // Vérifiez si le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        // Si la pagination est spécifiée, utilisez la version paginée
        if (pageable != null) {
            return productRepository.findByStoreId(storeId, pageable)
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        // Sinon, récupérez tous les produits du magasin
        return productRepository.findByStoreId(storeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    public List<ProductDto> getProductsByStoreId(Long storeId) {
        return getProductsByStoreId(storeId, null);
    }

    public ProductDto createProductForVendor(ProductDto productDto, String vendorUsername) {
        // 1. Trouver le vendeur par son username
        User vendor = userRepository.findByEmail(vendorUsername) // ou findByUsername selon votre implémentation
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // 2. Vérifier que le vendeur a le bon rôle si nécessaire

        // 3. Créer le produit
        Product product = new Product();
        product.setTitle(productDto.getTitle());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        // ... autres champs
        product.setVendor(vendor);

        // 4. Sauvegarder
        Product savedProduct = productRepository.save(product);

        // 5. Retourner le DTO
        return mapToDto(savedProduct);
    }
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getImageUrl() != null) product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (dto.getStoreId() != null) {
            Store store = storeRepository.findById(dto.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
            product.setStore(store);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productRepository.delete(product);
    }

    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public ProductDto createProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        User vendor = null;
        if (dto.getVendorId() != null) {
            vendor = userRepository.findById(dto.getVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        }

        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        product.setStore(store);

        if (vendor != null) {
            product.setVendor(vendor);
        }

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getVendorProductsByEmail(String email, int page, int size) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Store> stores = storeRepository.findByVendorId(vendor.getId());

        return stores.stream()
                .flatMap(store -> productRepository.findByStore(store).stream())
                .skip(page * size)
                .limit(size)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<Product> getProductsByVendorEmail(String email) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return storeRepository.findByVendorId(vendor.getId())
                .stream()
                .flatMap(store -> productRepository.findByStore(store).stream())
                .collect(Collectors.toList());
    }
    // À ajouter à ProductService.java
    public List<Product> getProductsByVendorUsername(String username) {
        // Puisque l'username est l'email dans votre implémentation
        return getProductsByVendorEmail(username);
    }


    public ProductDto mapToDto(Product product) {
        return new ProductDto(product);
    }
    public List<ProductDto> getProductsWithPromotions() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .filter(product -> product.getDiscountedPrice() != null &&
                        product.getDiscountedPrice().compareTo(BigDecimal.ZERO) > 0 &&
                        product.getDiscountedPrice().compareTo(product.getPrice()) < 0)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les produits qui n'ont pas de prix réduit (pas en promotion)
     */
    public List<ProductDto> getProductsWithoutPromotions() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .filter(product -> product.getDiscountedPrice() == null ||
                        product.getDiscountedPrice().compareTo(BigDecimal.ZERO) <= 0 ||
                        product.getDiscountedPrice().compareTo(product.getPrice()) >= 0)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

}