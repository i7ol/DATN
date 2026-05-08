package com.datn.shopdatabase.enums;

public enum ReturnType {
    RETURN("Đổi trả hàng"),
    EXCHANGE("Đổi sản phẩm khác"),
    WARRANTY("Bảo hành");

    private final String description;

    ReturnType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}