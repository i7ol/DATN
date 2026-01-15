package com.datn.shoppayment.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String username;
    private List<String> roles;
}
