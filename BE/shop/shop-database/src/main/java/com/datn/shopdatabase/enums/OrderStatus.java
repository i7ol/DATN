package com.datn.shopdatabase.enums;

public enum OrderStatus {
    NEW("Mới đặt"),
    PENDING_PAYMENT("Đang chờ thanh toán"),
    CONFIRMED("Đã xác nhận"),
    PROCESSING("Đang xử lý"),
    PACKING("Đang đóng gói"),
    SHIPPING("Đang vận chuyển"),
    DELIVERED("Đã giao hàng"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}