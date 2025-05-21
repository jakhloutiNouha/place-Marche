package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.Category;
import com.marche.place.Marche.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    List<Category> findByVendor(User vendor);
}