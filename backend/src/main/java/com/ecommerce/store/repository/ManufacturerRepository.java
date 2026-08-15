package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Manufacturer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    List<Manufacturer> findByActiveTrueOrderBySortOrderAscNameAsc();
    Optional<Manufacturer> findBySlugAndActiveTrue(String slug);
    Optional<Manufacturer> findBySlug(String slug);
    Optional<Manufacturer> findByIdAndActiveTrue(Long id);
}
