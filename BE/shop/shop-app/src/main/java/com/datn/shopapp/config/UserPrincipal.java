package com.datn.shopapp.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserPrincipal {

    private Long id;
    private String username;
    private List<String> roles;
}
