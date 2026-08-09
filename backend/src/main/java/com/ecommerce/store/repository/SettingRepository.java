package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Setting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByKeyName(String keyName);
}
