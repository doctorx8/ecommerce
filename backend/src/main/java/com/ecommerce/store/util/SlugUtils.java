package com.ecommerce.store.util;

public final class SlugUtils {
    private SlugUtils() {}

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String slug = input.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.length() > 120 ? slug.substring(0, 120) : slug;
    }
}
