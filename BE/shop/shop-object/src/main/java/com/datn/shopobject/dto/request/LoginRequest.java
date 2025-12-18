package com.datn.shopobject.dto.request;


import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private Boolean rememberMe = false;
}