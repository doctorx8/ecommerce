package com.ecommerce.store.config;

import com.ecommerce.store.entity.*;
import com.ecommerce.store.enums.StockStatus;
import com.ecommerce.store.repository.CategoryRepository;
import com.ecommerce.store.repository.ManufacturerRepository;
import com.ecommerce.store.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogNavBootstrap {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ManufacturerRepository manufacturerRepository;

    public CatalogNavBootstrap(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ManufacturerRepository manufacturerRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.manufacturerRepository = manufacturerRepository;
    }

    @Transactional
    public void ensureElectronicsNav() {
        Category notebook = upsertCategory("Notebook", "notebook", "Laptops and ultrabooks", null, 1);
        Category pcAcc = upsertCategory(
                "PC & Accessories", "pc-accessories", "Desktops, monitors, and PC gear", null, 2);
        Category gaming = upsertCategory("Gaming", "gaming", "Consoles, gaming PCs, and gear", null, 3);
        Category phoneTablet = upsertCategory(
                "Smartphone & Tablet", "smartphone-tablet", "Phones and tablets", null, 4);
        Category tvAudio = upsertCategory("TV & Audio", "tv-audio", "TVs, headphones, and speakers", null, 5);
        Category smartHome = upsertCategory("Smart Home", "smart-home", "Connected home devices", null, 6);
        Category phones = upsertCategory("Phones", "phones", "Smartphones", phoneTablet, 1);
        Category tablets = upsertCategory("Tablets", "tablets", "Tablets", phoneTablet, 2);
        Category audio = upsertCategory("Audio", "audio", "Headphones and speakers", tvAudio, 1);

        Manufacturer apple = upsertManufacturer("Apple", "apple", 1);
        Manufacturer sony = upsertManufacturer("Sony", "sony", 2);
        Manufacturer samsung = upsertManufacturer("Samsung", "samsung", 3);
        Manufacturer dell = upsertManufacturer("Dell", "dell", 4);
        Manufacturer logitech = upsertManufacturer("Logitech", "logitech", 5);
        Manufacturer nintendo = upsertManufacturer("Nintendo", "nintendo", 6);
        Manufacturer google = upsertManufacturer("Google", "google", 7);
        Manufacturer bose = upsertManufacturer("Bose", "bose", 8);
        Manufacturer asus = upsertManufacturer("ASUS", "asus", 9);
        Manufacturer philips = upsertManufacturer("Philips", "philips", 10);

        // Existing demo SKUs — keep category links healthy on older DBs.
        linkProduct("macbook-air-m3", notebook);
        linkProduct("iphone-15", phoneTablet, phones);
        linkProduct("sony-wh-1000xm5", tvAudio, audio);

        // Notebook
        ensureProduct(
                "MacBook Air M3", "macbook-air-m3", "MBA-M3-256", "MLY33",
                "Thin, fast, and quiet laptop",
                "MacBook Air with M3 chip delivers exceptional performance in a fanless design.",
                "1099", "1199", 25, true, apple,
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=80",
                notebook);
        ensureProduct(
                "Dell XPS 13", "dell-xps-13", "XPS13-9340", "XPS13",
                "Premium 13-inch Windows ultrabook",
                "A compact aluminum notebook with a sharp display, long battery life, and everyday speed for work and travel.",
                "999", "1149", 30, true, dell,
                "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=1200&q=80",
                notebook);
        ensureProduct(
                "ASUS Zenbook 14", "asus-zenbook-14", "UX3405", "Zenbook14",
                "Lightweight OLED notebook",
                "Zenbook 14 balances a vivid OLED panel with a slim chassis — ideal for students and creators on the move.",
                "849", "949", 40, false, asus,
                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=80",
                notebook);

        // PC & Accessories
        ensureProduct(
                "Logitech MX Master 3S", "logitech-mx-master-3s", "MX-MASTER-3S", "910-006556",
                "Precision wireless mouse",
                "Ergonomic productivity mouse with MagSpeed scrolling, quiet clicks, and multi-device pairing.",
                "99", "119", 120, false, logitech,
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=1200&q=80",
                pcAcc);
        ensureProduct(
                "Dell UltraSharp 27\" Monitor", "dell-ultrasharp-27", "U2723QE", "U2723QE",
                "4K USB-C productivity display",
                "27-inch UltraSharp monitor with crisp 4K resolution and a single USB-C cable for laptop docking.",
                "579", "649", 35, false, dell,
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=1200&q=80",
                pcAcc);
        ensureProduct(
                "Samsung T7 1TB SSD", "samsung-t7-1tb", "MU-PC1T0", "T7-1TB",
                "Portable USB-C SSD",
                "Fast external SSD for backups, photo libraries, and taking projects between machines.",
                "109", "139", 80, false, samsung,
                "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=1200&q=80",
                pcAcc);

        // Gaming
        ensureProduct(
                "PlayStation 5", "playstation-5", "PS5-DISC", "CFI-2016",
                "Sony next-gen console",
                "PlayStation 5 with ultra-high speed SSD, haptic feedback, and a huge catalog of exclusives.",
                "499", "549", 20, true, sony,
                "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?auto=format&fit=crop&w=1200&q=80",
                gaming);
        ensureProduct(
                "Nintendo Switch OLED", "nintendo-switch-oled", "SWITCH-OLED", "HEG-001",
                "Portable and docked gaming",
                "Switch OLED with a vivid 7-inch screen for handheld play and TV docking at home.",
                "349", null, 45, true, nintendo,
                "https://images.unsplash.com/photo-1578303512597-81e6d8c88863?auto=format&fit=crop&w=1200&q=80",
                gaming);
        ensureProduct(
                "ASUS ROG Ally", "asus-rog-ally", "ROG-ALLY-Z1E", "RC71L",
                "Windows handheld gaming PC",
                "Portable ROG Ally with AMD Z1 Extreme power for AAA titles on the go.",
                "599", "699", 18, false, asus,
                "https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?auto=format&fit=crop&w=1200&q=80",
                gaming);
        ensureProduct(
                "Logitech G Pro X Headset", "logitech-g-pro-x", "G-PRO-X", "981-000817",
                "Tournament-ready gaming headset",
                "Wired Pro X headset with BLUE VO!CE mic tech and interchangeable earpads for long sessions.",
                "129", "149", 60, false, logitech,
                "https://images.unsplash.com/photo-1599669454699-248893623440?auto=format&fit=crop&w=1200&q=80",
                gaming, pcAcc);

        // Smartphone & Tablet
        ensureProduct(
                "iPhone 15", "iphone-15", "IPHONE-15-128", "A3090",
                "Latest Apple smartphone",
                "The iPhone 15 features a durable design, advanced camera system, and all-day battery life.",
                "799", "899", 50, true, apple,
                "https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=1200&q=80",
                phoneTablet, phones);
        ensureProduct(
                "Samsung Galaxy S24", "samsung-galaxy-s24", "SM-S921", "S24-128",
                "Flagship Android phone",
                "Galaxy S24 with a bright display, versatile cameras, and Galaxy AI features for everyday speed.",
                "749", "849", 55, true, samsung,
                "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?auto=format&fit=crop&w=1200&q=80",
                phoneTablet, phones);
        ensureProduct(
                "Google Pixel 8", "google-pixel-8", "PIXEL-8-128", "GZPF0",
                "Pure Android camera phone",
                "Pixel 8 brings Google Tensor performance, clean software updates, and excellent computational photography.",
                "599", "699", 40, false, google,
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=1200&q=80",
                phoneTablet, phones);
        ensureProduct(
                "iPad Air M2", "ipad-air-m2", "IPAD-AIR-M2", "MM9E3",
                "Thin tablet for work and media",
                "iPad Air with M2 chip — great for note-taking, streaming, and light creative work with Apple Pencil support.",
                "599", "649", 35, true, apple,
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=1200&q=80",
                phoneTablet, tablets);

        // TV & Audio
        ensureProduct(
                "Sony WH-1000XM5", "sony-wh-1000xm5", "WH-1000XM5", "WH1000XM5",
                "Industry-leading noise canceling headphones",
                "Premium wireless headphones with exceptional noise cancellation and sound quality.",
                "348", "399", 100, false, sony,
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=80",
                tvAudio, audio);
        ensureProduct(
                "Bose QuietComfort Ultra", "bose-qc-ultra", "QC-ULTRA", "890357-0010",
                "Immersive noise-canceling headphones",
                "QuietComfort Ultra headphones with deep ANC, spatial audio modes, and all-day comfort.",
                "429", "479", 40, true, bose,
                "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=80",
                tvAudio, audio);
        ensureProduct(
                "Apple AirPods Pro 2", "airpods-pro-2", "AIRPODS-PRO-2", "MTJV3",
                "Active noise canceling earbuds",
                "AirPods Pro with Adaptive Audio, USB-C charging, and seamless Apple device pairing.",
                "249", "279", 90, true, apple,
                "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=1200&q=80",
                tvAudio, audio);
        ensureProduct(
                "Sony Bravia 55\" 4K TV", "sony-bravia-55", "XR-55X90L", "X90L-55",
                "4K Google TV with vivid picture",
                "55-inch Bravia 4K TV with Cognitive Processor XR and built-in Google TV streaming apps.",
                "898", "1099", 15, false, sony,
                "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?auto=format&fit=crop&w=1200&q=80",
                tvAudio);

        // Smart Home
        ensureProduct(
                "Google Nest Hub", "google-nest-hub", "NEST-HUB-2", "GA01331",
                "Smart display for home control",
                "Nest Hub shows calendars, cameras, and media while controlling compatible smart-home devices by voice.",
                "99", "129", 70, false, google,
                "https://images.unsplash.com/photo-1558089687-f282ffcbc0d4?auto=format&fit=crop&w=1200&q=80",
                smartHome);
        ensureProduct(
                "Apple HomePod mini", "homepod-mini", "HOMEPOD-MINI", "MY5H2",
                "Compact smart speaker",
                "HomePod mini fills a room with rich 360° audio and works as a Siri hub for HomeKit devices.",
                "99", null, 65, false, apple,
                "https://images.unsplash.com/photo-1589492477829-5e65395b66cc?auto=format&fit=crop&w=1200&q=80",
                smartHome);
        ensureProduct(
                "Philips Hue Starter Kit", "philips-hue-starter", "HUE-E27-KIT", "HueWhite",
                "Smart lighting starter pack",
                "Hue white bulbs and bridge to schedule scenes, dim from your phone, and automate rooms.",
                "79", "99", 50, false, philips,
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=1200&q=80",
                smartHome);
        ensureProduct(
                "Samsung Smart Monitor M8", "samsung-smart-monitor-m8", "M8-32", "LS32BM80",
                "Smart monitor for work and streaming",
                "32-inch Smart Monitor with apps, wireless DeX, and a clean desk-friendly design.",
                "649", "749", 22, false, samsung,
                "https://images.unsplash.com/photo-1585792187664-6b1c9f2c2e2f?auto=format&fit=crop&w=1200&q=80",
                smartHome, pcAcc);
    }

    private void linkProduct(String slug, Category... categories) {
        productRepository.findBySlugAndActiveTrue(slug).ifPresent(p -> {
            p.getCategories().size();
            for (Category c : categories) {
                p.getCategories().add(c);
            }
            productRepository.save(p);
        });
    }

    private void ensureProduct(
            String name,
            String slug,
            String sku,
            String model,
            String shortDesc,
            String description,
            String price,
            String compareAt,
            int qty,
            boolean featured,
            Manufacturer manufacturer,
            String imageUrl,
            Category... categories) {
        if (productRepository.findBySlug(slug).isPresent() || productRepository.findBySku(sku).isPresent()) {
            linkProduct(slug, categories);
            return;
        }

        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setSku(sku);
        p.setModel(model);
        p.setShortDesc(shortDesc);
        p.setDescription(description);
        p.setPrice(new BigDecimal(price));
        if (compareAt != null) {
            p.setCompareAtPrice(new BigDecimal(compareAt));
        }
        p.setQuantity(qty);
        p.setStockStatus(StockStatus.IN_STOCK);
        p.setFeatured(featured);
        p.setManufacturer(manufacturer);
        p.getCategories().addAll(List.of(categories));

        ProductImage image = new ProductImage();
        image.setProduct(p);
        image.setImage(imageUrl);
        image.setAlt(name);
        image.setSortOrder(0);
        p.getImages().add(image);

        productRepository.save(p);
    }

    private Category upsertCategory(String name, String slug, String description, Category parent, int sort) {
        Category c = categoryRepository.findBySlug(slug).orElseGet(Category::new);
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        c.setParent(parent);
        c.setSortOrder(sort);
        c.setActive(true);
        return categoryRepository.save(c);
    }

    private Manufacturer upsertManufacturer(String name, String slug, int sort) {
        Manufacturer m = manufacturerRepository.findBySlug(slug).orElseGet(Manufacturer::new);
        m.setName(name);
        m.setSlug(slug);
        m.setSortOrder(sort);
        m.setActive(true);
        return manufacturerRepository.save(m);
    }
}
