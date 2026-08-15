package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderBySortOrderAscNameAsc();
    Optional<Category> findBySlugAndActiveTrue(String slug);
    Optional<Category> findBySlug(String slug);
    Optional<Category> findByIdAndActiveTrue(Long id);
}
