// UserRepository.java
package com.marche.place.Marche.repository;

import com.marche.place.Marche.entity.User;
import com.marche.place.Marche.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRole(UserRole role, Pageable pageable);
}