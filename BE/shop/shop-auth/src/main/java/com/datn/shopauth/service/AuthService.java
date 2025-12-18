package com.datn.shopauth.service;

import com.datn.shopauth.dto.request.LoginRequest;
import com.datn.shopauth.dto.request.RefreshTokenRequest;
import com.datn.shopauth.dto.response.AuthResponse;
import com.datn.shopdatabase.enums.RoleEnums;
import com.datn.shopdatabase.repository.RoleRepository;
import com.datn.shopobject.dto.request.RegisterRequest;
import com.datn.shopobject.dto.response.RegisterResponse;
import com.datn.shopobject.dto.response.UserInfoResponse;
import com.datn.shopdatabase.entity.RoleEntity;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopdatabase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // 1. Kiểm tra username đã tồn tại chưa
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: Username {} already exists", request.getUsername());
            return RegisterResponse.builder()
                    .success(false)
                    .message("Username already exists")
                    .build();
        }

        // 2. Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            return RegisterResponse.builder()
                    .success(false)
                    .message("Email already exists")
                    .build();
        }

        // 3. Kiểm tra password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Registration failed: Password confirmation mismatch");
            return RegisterResponse.builder()
                    .success(false)
                    .message("Passwords do not match")
                    .build();
        }

        // 4. Tạo user entity
        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())

                .roles(new HashSet<>())
                .build();

        // 5. Gán role USER mặc định
        RoleEntity userRole = roleRepository.findById(RoleEnums.USER.name())
                .orElseGet(() -> {
                    // Nếu role USER chưa tồn tại, tạo mới
                    RoleEntity role = RoleEntity.builder()
                            .name(RoleEnums.USER.name())
                            .description("User role")
                            .build();
                    return roleRepository.save(role);
                });

        user.getRoles().add(userRole);

        // 6. Lưu user vào database
        UserEntity savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getUsername());

        return RegisterResponse.builder()
                .success(true)
                .message("Registration successful")
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();
    }

    // Kiểm tra username availability
    public boolean checkUsernameAvailability(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    // Kiểm tra email availability
    public boolean checkEmailAvailability(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Dùng AuthenticationManager để xác thực
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserEntity userEntity = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateAuthResponse(userEntity);
    }

    private AuthResponse generateAuthResponse(UserEntity userEntity) {
        List<String> roles = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        List<String> permissions = userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(userEntity.getUsername(), roles, permissions);
        String refreshToken = jwtService.generateRefreshToken(userEntity.getUsername());

        String sessionData = String.format("{\"roles\": %s, \"permissions\": %s}",
                roles.toString(), permissions.toString());
        jwtService.storeUserSession(userEntity.getUsername(), sessionData);

        log.info("User {} logged in successfully. Roles: {}", userEntity.getUsername(), roles);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .username(userEntity.getUsername())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        jwtService.invalidateRefreshToken(username);

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        List<String> permissions = userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        String newAccessToken = jwtService.generateAccessToken(username, roles, permissions);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        String sessionData = String.format("{\"roles\": %s, \"permissions\": %s}",
                roles.toString(), permissions.toString());
        jwtService.storeUserSession(username, sessionData);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .username(username)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        String username = jwtService.extractUsername(accessToken);

        long remainingTime = jwtService.getRemainingTime(accessToken);
        if (remainingTime > 0) {
            jwtService.blacklistToken(accessToken, remainingTime);
        }

        jwtService.invalidateRefreshToken(username);
        jwtService.invalidateUserSession(username);

        log.info("User {} logged out successfully. Access token blacklisted.", username);
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
    public boolean checkPermission(String token, String permission) {
        String username = jwtService.extractUsername(token);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasPermission = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(p -> p.getName().equals(permission));

        return hasPermission;
    }

    public UserInfoResponse getCurrentUser(String token) {
        String username = jwtService.extractUsername(token);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}