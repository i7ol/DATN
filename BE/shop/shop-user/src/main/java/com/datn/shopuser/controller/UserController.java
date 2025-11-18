package com.datn.shopuser.controller;


import com.datn.shopcore.dto.ApiResponse;
import com.datn.shopuser.dto.request.UserCreationRequest;
import com.datn.shopuser.dto.request.UserUpdateRequest;
import com.datn.shopuser.dto.response.UserResponse;
import com.datn.shopcore.entity.User;
import com.datn.shopuser.mapper.UserMapper;
import com.datn.shopcore.repository.UserRepository;
import com.datn.shopuser.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserController {
    final UserRepository userRepository;
    final UserService userService;
    final UserMapper userMapper;
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody @Valid UserCreationRequest request) {
        User createdUser = userService.createUser(request);

        ApiResponse<User> response = ApiResponse.<User>builder()
                .code(200)
                .message("User created successfully")
                .result(createdUser)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable("id") Long id) {
        UserResponse user = userService.getUser(id);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .code(200)
                        .message("User retrieved successfully")
                        .result(user)
                        .build()
        );
    }

    @PutMapping("/{id}")
    UserResponse updateUser(@PathVariable("id") Long id , @RequestBody UserUpdateRequest request){
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(200)
                .message("User has been deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

}

