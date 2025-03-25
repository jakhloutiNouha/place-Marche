package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.ProductDto;
import com.marche.place.Marche.entity.Category;
import com.marche.place.Marche.entity.Product;
import com.marche.place.Marche.entity.Store;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.CategoryRepository;
import com.marche.place.Marche.repository.ProductRepository;
import com.marche.place.Marche.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public ProductDto createProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        Product product = new Product();
        product.setTitle(dto.getTitle());  // Correction : Remplace "setName" par "setTitle"
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        product.setStore(store);

        product = productRepository.save(product);
        return mapToDto(product);
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));
        return mapToDto(product);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));
        productRepository.delete(product);
    }

    private ProductDto mapToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStock(),
                product.getCategory().getId(),
                product.getStore().getId()
        );
    }
}
