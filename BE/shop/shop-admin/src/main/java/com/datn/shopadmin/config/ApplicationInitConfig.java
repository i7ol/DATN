    package com.datn.shopadmin.config;


    import com.datn.shopdatabase.entity.RoleEntity;
    import com.datn.shopdatabase.entity.UserEntity;
    import com.datn.shopdatabase.enums.RoleEnums;
    import com.datn.shopdatabase.repository.RoleRepository;
    import com.datn.shopdatabase.repository.UserRepository;
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
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Slf4j
    public class ApplicationInitConfig {

        final PasswordEncoder passwordEncoder;
        final RoleRepository roleRepository;
        final UserRepository userRepository;

        @Bean
        ApplicationRunner applicationRunner() {
            return args -> {
                RoleEntity adminRole = roleRepository.findById(RoleEnums.ADMIN.name())
                        .orElseGet(() -> {
                            RoleEntity role = RoleEntity.builder()
                                    .name(RoleEnums.ADMIN.name())
                                    .description("Administrator role")
                                    .build();
                            return roleRepository.save(role);
                        });

                if (userRepository.findByUsername("admin").isEmpty()) {
                    Set<RoleEntity> roles = new HashSet<>();
                    roles.add(adminRole);

                    UserEntity admin = UserEntity.builder()
                            .username("admin")
                            .password(passwordEncoder.encode("admin"))
                            .email("admin@example.com")
                            .roles(roles)
                            .build();

                    userRepository.save(admin);
                    log.warn("Admin user has been created with default password: 'admin'. Please change it!");
                } else {
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

