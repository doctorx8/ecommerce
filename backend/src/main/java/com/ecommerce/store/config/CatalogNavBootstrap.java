package com.ecommerce.store.config;

import com.ecommerce.store.entity.Category;
import com.ecommerce.store.repository.CategoryRepository;
import com.ecommerce.store.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogNavBootstrap {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogNavBootstrap(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void ensureElectronicsNav() {
        Category notebook = upsertCategory("Notebook", "notebook", "Laptops and ultrabooks", null, 1);
        upsertCategory("PC & Accessories", "pc-accessories", "Desktops, monitors, and PC gear", null, 2);
        upsertCategory("Gaming", "gaming", "Consoles, gaming PCs, and gear", null, 3);
        Category phoneTablet = upsertCategory(
                "Smartphone & Tablet", "smartphone-tablet", "Phones and tablets", null, 4);
        Category tvAudio = upsertCategory("TV & Audio", "tv-audio", "TVs, headphones, and speakers", null, 5);
        upsertCategory("Smart Home", "smart-home", "Connected home devices", null, 6);
        Category phones = upsertCategory("Phones", "phones", "Smartphones", phoneTablet, 1);
        upsertCategory("Tablets", "tablets", "Tablets", phoneTablet, 2);
        Category audio = upsertCategory("Audio", "audio", "Headphones and speakers", tvAudio, 1);

        productRepository.findBySlugAndActiveTrue("macbook-air-m3").ifPresent(p -> {
            p.getCategories().size();
            p.getCategories().add(notebook);
            productRepository.save(p);
        });
        productRepository.findBySlugAndActiveTrue("iphone-15").ifPresent(p -> {
            p.getCategories().size();
            p.getCategories().add(phoneTablet);
            p.getCategories().add(phones);
            productRepository.save(p);
        });
        productRepository.findBySlugAndActiveTrue("sony-wh-1000xm5").ifPresent(p -> {
            p.getCategories().size();
            p.getCategories().add(tvAudio);
            p.getCategories().add(audio);
            productRepository.save(p);
        });
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
}
