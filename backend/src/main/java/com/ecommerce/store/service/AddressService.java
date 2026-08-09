package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.AccountDtos.AddressRequest;
import com.ecommerce.store.entity.Address;
import com.ecommerce.store.entity.Customer;
import com.ecommerce.store.repository.AddressRepository;
import com.ecommerce.store.repository.CustomerRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public AddressService(AddressRepository addressRepository, CustomerRepository customerRepository) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(Long customerId) {
        return addressRepository.findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(customerId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> create(Long customerId, AddressRequest req) {
        Customer customer = customerRepository.findById(customerId)
                .filter(Customer::isActive)
                .orElseThrow(() -> new ApiException("Customer not found", HttpStatus.NOT_FOUND));

        boolean makeDefault = Boolean.TRUE.equals(req.isDefault())
                || addressRepository.findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(customerId).isEmpty();

        if (makeDefault) {
            clearDefaults(customerId);
        }

        Address address = new Address();
        address.setCustomer(customer);
        apply(address, req, makeDefault);
        return toMap(addressRepository.save(address));
    }

    @Transactional
    public Map<String, Object> update(Long customerId, Long addressId, AddressRequest req) {
        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

        boolean makeDefault = Boolean.TRUE.equals(req.isDefault());
        if (makeDefault) {
            clearDefaults(customerId);
        }
        apply(address, req, makeDefault || address.isDefaultAddress());
        return toMap(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long customerId, Long addressId) {
        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));
        addressRepository.delete(address);
    }

    private void clearDefaults(Long customerId) {
        for (Address existing : addressRepository.findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(customerId)) {
            if (existing.isDefaultAddress()) {
                existing.setDefaultAddress(false);
                addressRepository.save(existing);
            }
        }
    }

    private void apply(Address address, AddressRequest req, boolean isDefault) {
        address.setFirstName(req.firstName());
        address.setLastName(req.lastName());
        address.setCompany(req.company());
        address.setAddress1(req.address1());
        address.setAddress2(req.address2());
        address.setCity(req.city());
        address.setPostcode(req.postcode());
        address.setCountry(req.country());
        address.setZone(req.zone());
        address.setDefaultAddress(isDefault);
    }

    private Map<String, Object> toMap(Address a) {
        Map<String, Object> am = new LinkedHashMap<>();
        am.put("id", a.getId());
        am.put("firstName", a.getFirstName());
        am.put("lastName", a.getLastName());
        am.put("company", a.getCompany());
        am.put("address1", a.getAddress1());
        am.put("address2", a.getAddress2());
        am.put("city", a.getCity());
        am.put("postcode", a.getPostcode());
        am.put("country", a.getCountry());
        am.put("zone", a.getZone());
        am.put("isDefault", a.isDefaultAddress());
        return am;
    }
}
