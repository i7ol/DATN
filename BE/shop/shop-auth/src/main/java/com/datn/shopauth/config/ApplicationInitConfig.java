package com.datn.shopauth.config;

import com.datn.shopcore.entity.Role;
import com.datn.shopcore.entity.User;
import com.datn.shopcore.enums.RoleEnums;
import com.datn.shopcore.repository.RoleRepository;
import com.datn.shopcore.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {

            // Đảm bảo role ADMIN luôn tồn tại trong DB
            Role adminRole = roleRepository.findById(RoleEnums.ADMIN.name())
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name(RoleEnums.ADMIN.name())
                                .description("Administrator role")
                                .build();
                        return roleRepository.save(role);
                    });

            // Kiểm tra nếu admin chưa tồn tại
            if (userRepository.findByUsername("admin").isEmpty()) {
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);

                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@example.com")
                        .roles(roles)
                        .build();

                userRepository.save(admin);
                log.warn("Admin user has been created with default password: 'admin'. Please change it!");
            } else {
                // Nếu đã tồn tại, đảm bảo admin vẫn có role ADMIN
                userRepository.findByUsername("admin").ifPresent(existingAdmin -> {
                    if (existingAdmin.getRoles() == null || existingAdmin.getRoles().isEmpty()) {
                        existingAdmin.setRoles(Set.of(adminRole));
                        userRepository.save(existingAdmin);
                        log.info("Existing admin was missing roles, now restored with ADMIN role.");
                    }
                });
            }
        };
    }
}
