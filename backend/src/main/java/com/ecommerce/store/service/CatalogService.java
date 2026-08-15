package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.CatalogDtos.*;
import com.ecommerce.store.entity.*;
import com.ecommerce.store.enums.StockStatus;
import com.ecommerce.store.repository.*;
import com.ecommerce.store.util.SlugUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ProductRepository productRepository;

    public CatalogService(
            CategoryRepository categoryRepository,
            ManufacturerRepository manufacturerRepository,
            ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Object listCategories(boolean tree) {
        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc();
        List<Map<String, Object>> flat = categories.stream().map(this::toCategoryMap).toList();
        if (!tree) {
            return flat;
        }
        Map<Long, Map<String, Object>> nodes = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> cat : flat) {
            cat.put("children", new ArrayList<Map<String, Object>>());
            nodes.put((Long) cat.get("id"), cat);
        }
        for (Category cat : categories) {
            Map<String, Object> node = nodes.get(cat.getId());
            if (cat.getParent() != null && nodes.containsKey(cat.getParent().getId())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children =
                        (List<Map<String, Object>>) nodes.get(cat.getParent().getId()).get("children");
                children.add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCategory(String idOrSlug) {
        Category category = findCategory(idOrSlug);
        Map<String, Object> map = toCategoryMap(category);
        map.put("children", category.getChildren().stream()
                .filter(Category::isActive)
                .sorted(Comparator.comparingInt(Category::getSortOrder))
                .map(this::toCategoryMap)
                .toList());
        return map;
    }

    @Transactional
    public Map<String, Object> createCategory(CategoryRequest req) {
        Category category = new Category();
        applyCategory(category, req);
        return toCategoryMap(categoryRepository.save(category));
    }

    @Transactional
    public Map<String, Object> updateCategory(Long id, CategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
        applyCategory(category, req);
        return toCategoryMap(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ApiException("Category not found", HttpStatus.NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listManufacturers() {
        return manufacturerRepository.findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream().map(this::toManufacturerMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getManufacturer(String idOrSlug) {
        return toManufacturerMap(findManufacturer(idOrSlug));
    }

    @Transactional
    public Map<String, Object> createManufacturer(ManufacturerRequest req) {
        Manufacturer m = new Manufacturer();
        m.setName(req.name());
        m.setSlug(req.slug() != null ? req.slug() : slugify(req.name()));
        m.setImage(req.image());
        m.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        m.setActive(req.active() == null || req.active());
        return toManufacturerMap(manufacturerRepository.save(m));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listProducts(
            int page, int limit, String search, String category, String manufacturer,
            Boolean featured, Boolean inStock, Boolean onSale, String sort,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (Boolean.TRUE.equals(featured)) {
                predicates.add(cb.isTrue(root.get("featured")));
            }
            if (Boolean.TRUE.equals(inStock)) {
                predicates.add(cb.equal(root.get("stockStatus"), StockStatus.IN_STOCK));
                predicates.add(cb.greaterThan(root.get("quantity"), 0));
            }
            if (Boolean.TRUE.equals(onSale)) {
                predicates.add(cb.isNotNull(root.get("compareAtPrice")));
                predicates.add(cb.greaterThan(root.get("compareAtPrice"), root.get("price")));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (category != null && !category.isBlank()) {
                var catJoin = root.join("categories", JoinType.INNER);
                predicates.add(cb.or(
                        cb.equal(catJoin.get("slug"), category),
                        isLong(category) ? cb.equal(catJoin.get("id"), Long.parseLong(category)) : cb.disjunction()
                ));
            }
            if (manufacturer != null && !manufacturer.isBlank()) {
                var manJoin = root.join("manufacturer", JoinType.INNER);
                predicates.add(cb.or(
                        cb.equal(manJoin.get("slug"), manufacturer),
                        isLong(manufacturer) ? cb.equal(manJoin.get("id"), Long.parseLong(manufacturer)) : cb.disjunction()
                ));
            }
            query.distinct(true);
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Sort sortObj = switch (sort == null ? "newest" : sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name", "name_asc" -> Sort.by("name").ascending();
            case "name_desc" -> Sort.by("name").descending();
            case "oldest" -> Sort.by("createdAt").ascending();
            case "featured" -> Sort.by("featured").descending().and(Sort.by("createdAt").descending());
            case "popular", "views" -> Sort.by("viewCount").descending().and(Sort.by("createdAt").descending());
            case "catalog", "sort_order" -> Sort.by("sortOrder").ascending().and(Sort.by("name").ascending());
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(limit, 1), 100), sortObj);
        Page<Product> result = productRepository.findAll(spec, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", result.getContent().stream().map(this::toProductSummary).toList());
        response.put("page", page);
        response.put("limit", pageable.getPageSize());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @Transactional
    public Map<String, Object> getProduct(String idOrSlug) {
        Product product = (isLong(idOrSlug)
                ? productRepository.findByIdAndActiveTrue(Long.parseLong(idOrSlug))
                : productRepository.findBySlugAndActiveTrue(idOrSlug))
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        // Touch lazy collections inside the transaction.
        product.getImages().size();
        product.getOptions().forEach(o -> o.getValues().size());
        product.getCategories().size();
        if (product.getManufacturer() != null) {
            product.getManufacturer().getName();
        }
        product.setViewCount(product.getViewCount() + 1);
        return toProductDetail(product);
    }

    @Transactional
    public Map<String, Object> createProduct(ProductRequest req) {
        Product product = new Product();
        applyProduct(product, req, true);
        return toProductDetail(productRepository.save(product));
    }

    @Transactional
    public Map<String, Object> updateProduct(Long id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        applyProduct(product, req, false);
        return toProductDetail(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ApiException("Product not found", HttpStatus.NOT_FOUND);
        }
        productRepository.deleteById(id);
    }

    private void applyCategory(Category category, CategoryRequest req) {
        category.setName(req.name());
        category.setSlug(req.slug() != null ? req.slug() : slugify(req.name()));
        category.setDescription(req.description());
        category.setImage(req.image());
        category.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        category.setActive(req.active() == null || req.active());
        category.setMetaTitle(req.metaTitle());
        category.setMetaDesc(req.metaDesc());
        if (req.parentId() != null) {
            category.setParent(categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new ApiException("Parent category not found", HttpStatus.BAD_REQUEST)));
        } else {
            category.setParent(null);
        }
    }

    private void applyProduct(Product product, ProductRequest req, boolean creating) {
        product.setName(req.name());
        product.setSlug(req.slug() != null ? req.slug() : slugify(req.name()));
        product.setSku(req.sku());
        product.setModel(req.model());
        product.setDescription(req.description());
        product.setShortDesc(req.shortDesc());
        product.setPrice(req.price());
        product.setCompareAtPrice(req.compareAtPrice());
        product.setCost(req.cost());
        if (req.quantity() != null || creating) {
            product.setQuantity(req.quantity() != null ? req.quantity() : 0);
        }
        if (req.stockStatus() != null) {
            product.setStockStatus(StockStatus.valueOf(req.stockStatus()));
        }
        product.setWeight(req.weight());
        product.setMinimum(req.minimum() != null ? req.minimum() : 1);
        product.setSubtractStock(req.subtractStock() == null || req.subtractStock());
        product.setActive(req.active() == null || req.active());
        product.setFeatured(Boolean.TRUE.equals(req.featured()));
        product.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        product.setMetaTitle(req.metaTitle());
        product.setMetaDesc(req.metaDesc());

        if (req.manufacturerId() != null) {
            product.setManufacturer(manufacturerRepository.findById(req.manufacturerId())
                    .orElseThrow(() -> new ApiException("Manufacturer not found", HttpStatus.BAD_REQUEST)));
        } else if (creating) {
            product.setManufacturer(null);
        }

        if (req.categoryIds() != null) {
            product.getCategories().clear();
            for (Long categoryId : req.categoryIds()) {
                product.getCategories().add(categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ApiException("Category not found: " + categoryId, HttpStatus.BAD_REQUEST)));
            }
        }

        if (req.images() != null) {
            product.getImages().clear();
            int i = 0;
            for (ProductImageRequest imageReq : req.images()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImage(imageReq.image());
                image.setAlt(imageReq.alt());
                image.setSortOrder(imageReq.sortOrder() != null ? imageReq.sortOrder() : i++);
                product.getImages().add(image);
            }
        }
    }

    private Category findCategory(String idOrSlug) {
        return (isLong(idOrSlug)
                ? categoryRepository.findByIdAndActiveTrue(Long.parseLong(idOrSlug))
                : categoryRepository.findBySlugAndActiveTrue(idOrSlug))
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
    }

    private Manufacturer findManufacturer(String idOrSlug) {
        return (isLong(idOrSlug)
                ? manufacturerRepository.findByIdAndActiveTrue(Long.parseLong(idOrSlug))
                : manufacturerRepository.findBySlugAndActiveTrue(idOrSlug))
                .orElseThrow(() -> new ApiException("Manufacturer not found", HttpStatus.NOT_FOUND));
    }

    private boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String slugify(String value) {
        String slug = SlugUtils.slugify(value);
        if (slug.isBlank()) {
            throw new ApiException("Invalid slug", HttpStatus.BAD_REQUEST);
        }
        return slug;
    }

    private Map<String, Object> toCategoryMap(Category c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("name", c.getName());
        map.put("slug", c.getSlug());
        map.put("description", c.getDescription());
        map.put("image", c.getImage());
        map.put("parentId", c.getParent() != null ? c.getParent().getId() : null);
        map.put("sortOrder", c.getSortOrder());
        map.put("isActive", c.isActive());
        map.put("metaTitle", c.getMetaTitle());
        map.put("metaDesc", c.getMetaDesc());
        return map;
    }

    private Map<String, Object> toManufacturerMap(Manufacturer m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("slug", m.getSlug());
        map.put("image", m.getImage());
        map.put("sortOrder", m.getSortOrder());
        map.put("isActive", m.isActive());
        return map;
    }

    private Map<String, Object> toProductSummary(Product p) {
        Map<String, Object> map = baseProduct(p);
        map.put("manufacturer", p.getManufacturer() != null ? toManufacturerMap(p.getManufacturer()) : null);
        map.put("images", p.getImages().stream().limit(1).map(this::toImageMap).toList());
        map.put("categories", p.getCategories().stream().map(this::toCategoryMap).toList());
        return map;
    }

    private Map<String, Object> toProductDetail(Product p) {
        Map<String, Object> map = baseProduct(p);
        map.put("manufacturer", p.getManufacturer() != null ? toManufacturerMap(p.getManufacturer()) : null);
        map.put("images", p.getImages().stream().map(this::toImageMap).toList());
        map.put("categories", p.getCategories().stream().map(this::toCategoryMap).toList());
        map.put("options", p.getOptions().stream().map(opt -> {
            Map<String, Object> om = new LinkedHashMap<>();
            om.put("id", opt.getId());
            om.put("name", opt.getName());
            om.put("required", opt.isRequired());
            om.put("sortOrder", opt.getSortOrder());
            om.put("values", opt.getValues().stream().map(v -> {
                Map<String, Object> vm = new LinkedHashMap<>();
                vm.put("id", v.getId());
                vm.put("name", v.getName());
                vm.put("priceModifier", v.getPriceModifier());
                vm.put("quantity", v.getQuantity());
                vm.put("sku", v.getSku());
                vm.put("sortOrder", v.getSortOrder());
                return vm;
            }).toList());
            return om;
        }).toList());
        return map;
    }

    private Map<String, Object> baseProduct(Product p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("slug", p.getSlug());
        map.put("sku", p.getSku());
        map.put("model", p.getModel());
        map.put("description", p.getDescription());
        map.put("shortDesc", p.getShortDesc());
        map.put("price", p.getPrice());
        map.put("compareAtPrice", p.getCompareAtPrice());
        map.put("quantity", p.getQuantity());
        map.put("stockStatus", p.getStockStatus().name());
        map.put("isActive", p.isActive());
        map.put("isFeatured", p.isFeatured());
        map.put("viewCount", p.getViewCount());
        map.put("createdAt", p.getCreatedAt());
        return map;
    }

    private Map<String, Object> toImageMap(ProductImage image) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", image.getId());
        map.put("image", image.getImage());
        map.put("alt", image.getAlt());
        map.put("sortOrder", image.getSortOrder());
        return map;
    }
}
