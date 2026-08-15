package com.ecommerce.store.config;

import com.ecommerce.store.entity.*;
import com.ecommerce.store.enums.UserRole;
import com.ecommerce.store.repository.*;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            AdminUserRepository adminUserRepository,
            CustomerRepository customerRepository,
            AddressRepository addressRepository,
            CouponRepository couponRepository,
            SettingRepository settingRepository,
            PasswordEncoder passwordEncoder,
            CatalogNavBootstrap catalogNavBootstrap) {
        return args -> {
            upsertSetting(settingRepository, "store_name", "Karwan");
            catalogNavBootstrap.ensureElectronicsNav();

            if (adminUserRepository.count() > 0) {
                return;
            }

            String hash = passwordEncoder.encode("password123");

            AdminUser admin = new AdminUser();
            admin.setEmail("admin@store.local");
            admin.setPasswordHash(hash);
            admin.setFirstName("Store");
            admin.setLastName("Admin");
            admin.setRole(UserRole.ADMIN);
            adminUserRepository.save(admin);

            Customer customer = new Customer();
            customer.setEmail("customer@store.local");
            customer.setPasswordHash(hash);
            customer.setFirstName("Jane");
            customer.setLastName("Doe");
            customer.setTelephone("+1-555-0100");
            customerRepository.save(customer);

            Address address = new Address();
            address.setCustomer(customer);
            address.setFirstName("Jane");
            address.setLastName("Doe");
            address.setAddress1("123 Market Street");
            address.setCity("San Francisco");
            address.setPostcode("94105");
            address.setCountry("US");
            address.setZone("CA");
            address.setDefaultAddress(true);
            addressRepository.save(address);

            Coupon coupon = new Coupon();
            coupon.setCode("WELCOME10");
            coupon.setName("Welcome 10% Off");
            coupon.setType("PERCENT");
            coupon.setDiscount(new BigDecimal("10"));
            coupon.setMinOrderTotal(new BigDecimal("50"));
            coupon.setMaxUses(1000);
            coupon.setActive(true);
            couponRepository.save(coupon);

            upsertSetting(settingRepository, "store_currency", "USD");
            upsertSetting(settingRepository, "store_email", "support@store.local");
        };
    }

    private static void upsertSetting(SettingRepository repo, String key, String value) {
        Setting setting = repo.findByKeyName(key).orElseGet(Setting::new);
        setting.setKeyName(key);
        setting.setValue(value);
        setting.setGroup("config");
        repo.save(setting);
    }
}
