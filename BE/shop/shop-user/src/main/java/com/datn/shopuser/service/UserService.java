    package com.datn.shopuser.service;


    import com.datn.shopcore.entity.Role;
    import com.datn.shopcore.enums.RoleEnums;
    import com.datn.shopcore.repository.RoleRepository;
    import com.datn.shopcore.exception.AppException;
    import com.datn.shopcore.exception.ErrorCode;
    import com.datn.shopuser.dto.request.UserCreationRequest;
    import com.datn.shopuser.dto.request.UserUpdateRequest;
    import com.datn.shopuser.dto.response.UserResponse;
    import com.datn.shopcore.entity.User;
    import com.datn.shopuser.mapper.UserMapper;
    import com.datn.shopcore.repository.UserRepository;
    import lombok.AccessLevel;
    import lombok.RequiredArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
    public class    UserService {
        UserRepository userRepository;
        RoleRepository roleRepository;
        UserMapper userMapper;
        PasswordEncoder passwordEncoder;


        public User createUser(UserCreationRequest request) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
            Role userRole = roleRepository.findById(RoleEnums.USER.name())
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .name(RoleEnums.USER.name())
                                .description("Default user role")
                                .build();
                        return roleRepository.save(newRole);
                    });

            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
           return userRepository.save(user);

        }

        public List<User> getUsers() {
            return userRepository.findAll();
        }


        public UserResponse getUser(Long id) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .roles(user.getRoles() != null
                            ? user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
                            : List.of())
                    .build();
        }

        public UserResponse updateUser(Long id, UserUpdateRequest request) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                user.setEmail(request.getEmail());
            }
            if (request.getPhone() != null) user.setPhone(request.getPhone());
            if (request.getAddress() != null) user.setAddress(request.getAddress());

            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                Set<Role> roles = request.getRoles().stream()
                        .map(roleName -> roleRepository.findById(roleName)
                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
                        .collect(Collectors.toSet());
                user.setRoles(roles);
            }

            userRepository.save(user);

            // build response manually
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .roles(user.getRoles() != null
                            ? user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
                            : List.of())
                    .build();
        }


//        public UserResponse updateUser(Long id, UserUpdateRequest request) {
//            User user = userRepository.findById(id)
//                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//            if (request.getPassword() != null && !request.getPassword().isBlank()) {
//                user.setPassword(passwordEncoder.encode(request.getPassword()));
//            }
//
//
//            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
//                Set<Role> roles = request.getRoles().stream()
//                        .map(roleName -> roleRepository.findById(roleName)
//                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
//                        .collect(Collectors.toSet());
//                user.setRoles(roles);
//            }
//
//            userMapper.updateUser(user, request);
//
//            userRepository.save(user);
//
//            return userMapper.toUserResponse(user);
//        }


        public void deleteUser(Long id) {
            userRepository.deleteById(id);
        }

}

