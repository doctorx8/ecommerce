package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlugAndActiveTrue(String slug);
    Optional<Product> findByIdAndActiveTrue(Long id);
    Optional<Product> findBySku(String sku);

    Page<Product> findByActiveTrue(Pageable pageable);
}
