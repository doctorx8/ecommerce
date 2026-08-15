package com.ecommerce.store.config;

import com.ecommerce.store.entity.*;
import com.ecommerce.store.enums.StockStatus;
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
            ManufacturerRepository manufacturerRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
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

            Manufacturer apple = manufacturer("Apple", "apple", 1);
            Manufacturer sony = manufacturer("Sony", "sony", 2);
            manufacturerRepository.save(apple);
            manufacturerRepository.save(sony);

            Category notebook = category("Notebook", "notebook", "Laptops and ultrabooks", null, 1);
            Category pcAcc = category("PC & Accessories", "pc-accessories", "Desktops, monitors, and PC gear", null, 2);
            Category gaming = category("Gaming", "gaming", "Consoles, gaming PCs, and gear", null, 3);
            Category phoneTablet = category("Smartphone & Tablet", "smartphone-tablet", "Phones and tablets", null, 4);
            Category tvAudio = category("TV & Audio", "tv-audio", "TVs, headphones, and speakers", null, 5);
            Category smartHome = category("Smart Home", "smart-home", "Connected home devices", null, 6);
            categoryRepository.save(notebook);
            categoryRepository.save(pcAcc);
            categoryRepository.save(gaming);
            categoryRepository.save(phoneTablet);
            categoryRepository.save(tvAudio);
            categoryRepository.save(smartHome);

            Category phones = category("Phones", "phones", "Smartphones", phoneTablet, 1);
            Category tablets = category("Tablets", "tablets", "Tablets", phoneTablet, 2);
            Category audio = category("Audio", "audio", "Headphones and speakers", tvAudio, 1);
            categoryRepository.save(phones);
            categoryRepository.save(tablets);
            categoryRepository.save(audio);

            Product iphone = product("iPhone 15", "iphone-15", "IPHONE-15-128", "A3090",
                    "Latest Apple smartphone",
                    "The iPhone 15 features a durable design, advanced camera system, and all-day battery life.",
                    new BigDecimal("799"), new BigDecimal("899"), 50, true, apple);
            iphone.getCategories().add(phones);
            iphone.getCategories().add(phoneTablet);
            addImage(iphone, "/images/iphone-15.jpg", "iPhone 15");
            ProductOption color = new ProductOption();
            color.setProduct(iphone);
            color.setName("Color");
            color.setRequired(true);
            ProductOptionValue black = optionValue(color, "Black", "0", 0);
            ProductOptionValue blue = optionValue(color, "Blue", "0", 1);
            ProductOptionValue pink = optionValue(color, "Pink", "20", 2);
            color.getValues().add(black);
            color.getValues().add(blue);
            color.getValues().add(pink);
            iphone.getOptions().add(color);
            productRepository.save(iphone);

            Product macbook = product("MacBook Air M3", "macbook-air-m3", "MBA-M3-256", "MLY33",
                    "Thin, fast, and quiet laptop",
                    "MacBook Air with M3 chip delivers exceptional performance in a fanless design.",
                    new BigDecimal("1099"), new BigDecimal("1199"), 25, true, apple);
            macbook.getCategories().add(notebook);
            addImage(macbook, "/images/macbook-air.jpg", "MacBook Air");
            productRepository.save(macbook);

            Product headphones = product("Sony WH-1000XM5", "sony-wh-1000xm5", "WH-1000XM5", null,
                    "Industry-leading noise canceling headphones",
                    "Premium wireless headphones with exceptional noise cancellation and sound quality.",
                    new BigDecimal("348"), null, 100, false, sony);
            headphones.getCategories().add(tvAudio);
            headphones.getCategories().add(audio);
            addImage(headphones, "/images/sony-headphones.jpg", "Sony Headphones");
            productRepository.save(headphones);

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

    private static Manufacturer manufacturer(String name, String slug, int sort) {
        Manufacturer m = new Manufacturer();
        m.setName(name);
        m.setSlug(slug);
        m.setSortOrder(sort);
        return m;
    }

    private static Category category(String name, String slug, String description, Category parent, int sort) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        c.setParent(parent);
        c.setSortOrder(sort);
        return c;
    }

    private static Product product(
            String name, String slug, String sku, String model, String shortDesc, String description,
            BigDecimal price, BigDecimal compareAt, int qty, boolean featured, Manufacturer manufacturer) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setSku(sku);
        p.setModel(model);
        p.setShortDesc(shortDesc);
        p.setDescription(description);
        p.setPrice(price);
        p.setCompareAtPrice(compareAt);
        p.setQuantity(qty);
        p.setStockStatus(StockStatus.IN_STOCK);
        p.setFeatured(featured);
        p.setManufacturer(manufacturer);
        return p;
    }

    private static void addImage(Product product, String path, String alt) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImage(path);
        image.setAlt(alt);
        image.setSortOrder(0);
        product.getImages().add(image);
    }

    private static ProductOptionValue optionValue(ProductOption option, String name, String modifier, int sort) {
        ProductOptionValue value = new ProductOptionValue();
        value.setProductOption(option);
        value.setName(name);
        value.setPriceModifier(new BigDecimal(modifier));
        value.setSortOrder(sort);
        return value;
    }

}
