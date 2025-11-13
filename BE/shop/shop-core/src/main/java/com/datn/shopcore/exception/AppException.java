package com.datn.shopcore.exception;


import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public AppException(ErrorCode errorCode, String message) {
        super(message); // Sử dụng thông báo tùy chỉnh cho RuntimeException
        this.errorCode = errorCode;
}}

