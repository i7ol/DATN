package com.datn.shopapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if ("admin".equals(username)) {
                return User.withUsername("admin")
                        .password("{noop}123456")
                        .roles("ADMIN")
                        .build();
            } else if ("user".equals(username)) {
                return User.withUsername("user")
                        .password("{noop}123456")
                        .roles("USER")
                        .build();
            }
            throw new UsernameNotFoundException("User not found: " + username);
        };
    }
}
