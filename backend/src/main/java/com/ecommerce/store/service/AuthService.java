package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.AccountDtos.ChangePasswordRequest;
import com.ecommerce.store.dto.AccountDtos.DeleteAccountRequest;
import com.ecommerce.store.dto.AccountDtos.UpdateProfileRequest;
import com.ecommerce.store.dto.AuthDtos.LoginRequest;
import com.ecommerce.store.dto.AuthDtos.RegisterRequest;
import com.ecommerce.store.entity.AdminUser;
import com.ecommerce.store.entity.Customer;
import com.ecommerce.store.repository.AdminUserRepository;
import com.ecommerce.store.repository.CartItemRepository;
import com.ecommerce.store.repository.CustomerRepository;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.security.JwtService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final AdminUserRepository adminUserRepository;
    private final CartItemRepository cartItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CustomerRepository customerRepository,
            AdminUserRepository adminUserRepository,
            CartItemRepository cartItemRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.adminUserRepository = adminUserRepository;
        this.cartItemRepository = cartItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        if (customerRepository.existsByEmail(req.email())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }
        Customer customer = new Customer();
        customer.setEmail(req.email());
        customer.setPasswordHash(passwordEncoder.encode(req.password()));
        customer.setFirstName(req.firstName());
        customer.setLastName(req.lastName());
        customer.setTelephone(req.telephone());
        customerRepository.save(customer);

        AuthUser user = new AuthUser(customer.getId(), customer.getEmail(), "customer", "CUSTOMER", customer.getPasswordHash());
        return Map.of("customer", toCustomerMap(customer), "token", jwtService.generateToken(user));
    }

    public Map<String, Object> loginCustomer(LoginRequest req) {
        Customer customer = customerRepository.findByEmail(req.email())
                .filter(Customer::isActive)
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(req.password(), customer.getPasswordHash())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        AuthUser user = new AuthUser(customer.getId(), customer.getEmail(), "customer", "CUSTOMER", customer.getPasswordHash());
        return Map.of("customer", toCustomerMap(customer), "token", jwtService.generateToken(user));
    }

    public Map<String, Object> loginAdmin(LoginRequest req) {
        AdminUser admin = adminUserRepository.findByEmail(req.email())
                .filter(AdminUser::isActive)
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(req.password(), admin.getPasswordHash())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        AuthUser user = new AuthUser(admin.getId(), admin.getEmail(), "admin", admin.getRole().name(), admin.getPasswordHash());
        Map<String, Object> adminMap = new LinkedHashMap<>();
        adminMap.put("id", admin.getId());
        adminMap.put("email", admin.getEmail());
        adminMap.put("firstName", admin.getFirstName());
        adminMap.put("lastName", admin.getLastName());
        adminMap.put("role", admin.getRole().name());
        return Map.of("admin", adminMap, "token", jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> me(Long customerId) {
        Customer customer = requireActiveCustomer(customerId);
        return toFullCustomerMap(customer);
    }

    @Transactional
    public Map<String, Object> updateProfile(Long customerId, UpdateProfileRequest req) {
        Customer customer = requireActiveCustomer(customerId);
        if (!customer.getEmail().equalsIgnoreCase(req.email())
                && customerRepository.existsByEmail(req.email())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }
        customer.setEmail(req.email());
        customer.setFirstName(req.firstName());
        customer.setLastName(req.lastName());
        customer.setTelephone(req.telephone());
        if (req.newsletter() != null) {
            customer.setNewsletter(req.newsletter());
        }
        customerRepository.save(customer);
        return toFullCustomerMap(customer);
    }

    @Transactional
    public void changePassword(Long customerId, ChangePasswordRequest req) {
        Customer customer = requireActiveCustomer(customerId);
        if (!passwordEncoder.matches(req.currentPassword(), customer.getPasswordHash())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        customer.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        customerRepository.save(customer);
    }

    @Transactional
    public void deleteAccount(Long customerId, DeleteAccountRequest req) {
        Customer customer = requireActiveCustomer(customerId);
        if (!passwordEncoder.matches(req.password(), customer.getPasswordHash())) {
            throw new ApiException("Password is incorrect", HttpStatus.BAD_REQUEST);
        }
        cartItemRepository.deleteByCustomerId(customerId);
        customer.setActive(false);
        customer.setEmail("deleted+" + customer.getId() + "@karwan.local");
        customer.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        customer.setTelephone(null);
        customer.setNewsletter(false);
        customerRepository.save(customer);
    }

    private Customer requireActiveCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .filter(Customer::isActive)
                .orElseThrow(() -> new ApiException("Customer not found", HttpStatus.NOT_FOUND));
    }

    private Map<String, Object> toFullCustomerMap(Customer customer) {
        Map<String, Object> map = toCustomerMap(customer);
        map.put("newsletter", customer.isNewsletter());
        map.put("createdAt", customer.getCreatedAt());
        map.put("addresses", customer.getAddresses().stream().map(a -> {
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
        }).toList());
        return map;
    }

    private Map<String, Object> toCustomerMap(Customer customer) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", customer.getId());
        map.put("email", customer.getEmail());
        map.put("firstName", customer.getFirstName());
        map.put("lastName", customer.getLastName());
        map.put("telephone", customer.getTelephone());
        return map;
    }
}
