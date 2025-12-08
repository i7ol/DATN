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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    // Tạo user mới
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

        RoleEntity userRole = roleRepository.findById(RoleEnums.USER.name())
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .name(RoleEnums.USER.name())
                                .description("Default user role")
                                .build()
                ));
        user.setRoles(Set.of(userRole));

        return userRepository.save(user);
    }

    // Lấy tất cả user
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }
    // Lấy User entity trực tiếp
    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    // Lấy tất cả User entity
    public List<UserEntity> getAllUserEntities() {
        return userRepository.findAll();
    }


    // Lấy 1 user theo id
    public UserResponse getUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return mapToResponse(user);
    }

    // Cập nhật user
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<RoleEntity> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findById(roleName)
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    // Xóa user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Chuyển entity -> DTO
    private UserResponse mapToResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(user.getRoles() != null
                        ? user.getRoles().stream().map(RoleEntity::getName).toList()
                        : List.of())
                .pushToken(user.getPushToken())
                .build();
    }
}
