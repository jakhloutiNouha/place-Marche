package com.marche.place.Marche.service;

import com.marche.place.Marche.dto.CategoryDto;
import com.marche.place.Marche.entity.Category;
import com.marche.place.Marche.exception.ConflictException;
import com.marche.place.Marche.exception.ResourceNotFoundException;
import com.marche.place.Marche.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDto createCategory(CategoryDto dto) {
        if(categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Nom de catégorie déjà utilisé");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

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

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
        return convertToDto(category);
    }

    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

        if(!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Nouveau nom déjà utilisé");
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return convertToDto(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        if(!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Catégorie non trouvée");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}