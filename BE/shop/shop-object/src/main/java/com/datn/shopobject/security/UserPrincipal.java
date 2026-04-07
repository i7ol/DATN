package com.datn.shopobject.security;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserPrincipal implements Serializable {

    private Long id;
    private String username;
    private List<String> roles;

    // Constructor chính (chỉ giữ 1 cái)
    public UserPrincipal(Long id, String username, List<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles != null ? List.copyOf(roles) : List.of();
    }
}