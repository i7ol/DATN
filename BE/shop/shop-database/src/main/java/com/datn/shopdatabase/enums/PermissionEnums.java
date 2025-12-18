
package com.datn.shopdatabase.enums;

import lombok.Getter;

@Getter
public enum PermissionEnums {
    // User permissions
    VIEW_PRODUCTS("view_products", "View products", "/api/user/products/**", "GET"),
    ADD_TO_CART("add_to_cart", "Add to cart", "/api/user/cart/add", "POST"),
    VIEW_CART("view_cart", "View cart", "/api/user/cart/**", "GET"),
    CREATE_ORDER("create_order", "Create order", "/api/user/orders", "POST"),

    // Admin permissions
    MANAGE_PRODUCTS("manage_products", "Manage products", "/api/admin/products/**", "ALL"),
    MANAGE_USERS("manage_users", "Manage users", "/api/admin/users/**", "ALL"),
    MANAGE_ORDERS("manage_orders", "Manage orders", "/api/admin/orders/**", "ALL"),
    MANAGE_CATEGORIES("manage_categories", "Manage categories", "/api/admin/categories/**", "ALL"),
    MANAGE_INVENTORY("manage_inventory", "Manage inventory", "/api/admin/inventory/**", "ALL"),

    // Common
    VIEW_PROFILE("view_profile", "View profile", "/api/**/profile", "GET"),
    UPDATE_PROFILE("update_profile", "Update profile", "/api/**/profile", "PUT");

    private final String name;
    private final String description;
    private final String endpointPattern;
    private final String httpMethod;

    PermissionEnums(String name, String description, String endpointPattern, String httpMethod) {
        this.name = name;
        this.description = description;
        this.endpointPattern = endpointPattern;
        this.httpMethod = httpMethod;
    }
}