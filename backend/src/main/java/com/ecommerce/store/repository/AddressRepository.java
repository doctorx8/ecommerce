package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(Long customerId);
    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);
}
