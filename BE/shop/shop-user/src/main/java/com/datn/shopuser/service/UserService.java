package com.datn.shopuser.service;


import com.datn.shopdatabase.entity.RoleEntity;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopdatabase.enums.RoleEnums;
import com.datn.shopdatabase.repository.RoleRepository;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.UserCreationRequest;
import com.datn.shopobject.dto.request.UserUpdateRequest;
import com.datn.shopobject.dto.response.UserResponse;
import com.datn.shopdatabase.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    /* ===================== QUERY ===================== */

    public UserResponse getUserById(Long id) {
        return mapToResponse(findUserEntity(id));
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /* ===================== COMMAND ===================== */

    public UserEntity createUser(UserCreationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRoles(Set.of(getDefaultUserRole()));

        return userRepository.save(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = findUserEntity(id);

        if (hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (hasText(request.getEmail())) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<RoleEntity> roles = request.getRoles().stream()
                    .map(this::getRoleByName)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.delete(findUserEntity(id));
    }

    /* ===================== PRIVATE ===================== */

    private UserEntity findUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private RoleEntity getDefaultUserRole() {
        return roleRepository.findById(RoleEnums.USER.name())
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .name(RoleEnums.USER.name())
                                .description("Default user role")
                                .build()
                ));
    }

    private RoleEntity getRoleByName(String roleName) {
        return roleRepository.findById(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private UserResponse mapToResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(
                        user.getRoles() == null
                                ? List.of()
                                : user.getRoles().stream()
                                .map(RoleEntity::getName)
                                .toList()
                )
                .pushToken(user.getPushToken())
                .build();
    }
}
