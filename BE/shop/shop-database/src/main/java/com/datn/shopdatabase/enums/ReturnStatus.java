package com.datn.shopdatabase.enums;

public enum ReturnStatus {
    PENDING("Chờ xử lý"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String description;

    ReturnStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
