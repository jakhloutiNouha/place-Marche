package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.CategoryDto;
import com.marche.place.Marche.entity.Category;
import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import com.marche.place.Marche.exception.ConflictException;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.CategoryRepository;
import com.marche.place.Marche.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Nom de catégorie déjà utilisé");
        }

        User vendor = getCurrentUser();

        if (!vendor.getRole().equals(UserRole.Vendor)) {
            throw new AccessDeniedException("Seuls les vendeurs peuvent créer des catégories.");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setVendor(vendor);

        try {
            return convertToDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Violation de contrainte unique");
        }
    }

    public CategoryDto createVendorCategory(CategoryDto dto, String vendorEmail) {
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur introuvable avec l'email : " + vendorEmail));

        if (!vendor.getRole().equals(UserRole.Vendor)) {
            throw new AccessDeniedException("Seuls les vendeurs peuvent créer des catégories.");
        }

        // Vérifier si le vendor a déjà une catégorie avec ce nom
        boolean existsForVendor = categoryRepository.findByVendor(vendor)
                .stream()
                .anyMatch(cat -> cat.getName().equalsIgnoreCase(dto.getName()));

        if (existsForVendor) {
            throw new ConflictException("Vous avez déjà une catégorie avec ce nom");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setVendor(vendor);

        try {
            return convertToDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Violation de contrainte unique");
        }
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

        User currentUser = getCurrentUser();

        if (currentUser.getRole().equals(UserRole.Vendor) &&
                (category.getVendor() == null || !category.getVendor().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Vous n'avez pas l'autorisation de supprimer cette catégorie.");
        }

        if (!category.getProducts().isEmpty()) {
            throw new ConflictException("Impossible de supprimer une catégorie contenant des produits.");
        }

        categoryRepository.delete(category);
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
        return convertToDto(category);
    }

    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

        User currentUser = getCurrentUser();

        if (currentUser.getRole().equals(UserRole.Vendor) &&
                (category.getVendor() == null || !category.getVendor().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Vous n'avez pas l'autorisation de modifier cette catégorie.");
        }

        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Nouveau nom déjà utilisé");
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        return convertToDto(categoryRepository.save(category));
    }

    public List<CategoryDto> getVendorCategories(String email) {
        User vendor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur introuvable avec l'email : " + email));

        if (!vendor.getRole().equals(UserRole.Vendor)) {
            throw new AccessDeniedException("Seuls les vendeurs peuvent accéder à cette ressource.");
        }

        return categoryRepository.findByVendor(vendor)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        if (category.getVendor() != null) {
            dto.setVendorId(category.getVendor().getId());
        }
        return dto;
    }

    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}