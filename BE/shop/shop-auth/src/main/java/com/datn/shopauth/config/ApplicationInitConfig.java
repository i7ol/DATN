package com.datn.shopauth.config;

import com.datn.shopdatabase.entity.RoleEntity;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopdatabase.enums.RoleEnums;
import com.datn.shopdatabase.repository.RoleRepository;
import com.datn.shopdatabase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            // Tạo role ADMIN nếu chưa có
            RoleEntity adminRole = roleRepository.findById(RoleEnums.ADMIN.name())
                    .orElseGet(() -> {
                        RoleEntity role = RoleEntity.builder()
                                .name(RoleEnums.ADMIN.name())
                                .description("Administrator role")
                                .build();
                        return roleRepository.save(role);
                    });

            // Tạo role USER nếu chưa có
            RoleEntity userRole = roleRepository.findById(RoleEnums.USER.name())
                    .orElseGet(() -> {
                        RoleEntity role = RoleEntity.builder()
                                .name(RoleEnums.USER.name())
                                .description("User role")
                                .build();
                        return roleRepository.save(role);
                    });

            // Tạo user admin nếu chưa có
            if (userRepository.findByUsername("admin").isEmpty()) {
                UserEntity admin = UserEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@admin.com")
                        .roles(new HashSet<>())
                        .build();
                admin.getRoles().add(userRole);
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
                log.warn("Admin user created with password: 'admin'");
            }

            // Tạo user thường nếu chưa có
            if (userRepository.findByUsername("user").isEmpty()) {
                UserEntity user = UserEntity.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user"))
                        .email("user@user.com")
                        .roles(new HashSet<>())
                        .build();
                user.getRoles().add(userRole);
                userRepository.save(user);
                log.warn("User created with password: 'user'");
            }
        };
    }
}