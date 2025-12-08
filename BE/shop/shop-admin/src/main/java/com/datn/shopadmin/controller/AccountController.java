package com.datn.shopadmin.controller;

import com.datn.shopdatabase.dto.ApiResponse;
import com.datn.shopdatabase.entity.RoleEntity;
import com.datn.shopdatabase.entity.UserEntity;
import com.datn.shopobject.dto.request.UserCreationRequest;
import com.datn.shopobject.dto.request.UserUpdateRequest;
import com.datn.shopobject.dto.response.UserResponse;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/account")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AccountController {
    final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid UserCreationRequest request) {
        UserEntity createdUser = userService.createUser(request);

        UserResponse responseData = toUserResponse(createdUser); // helper method mapping

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(200)
                .message("User created successfully")
                .result(responseData)
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

    private UserResponse toUserResponse(UserEntity user) {
        if (user == null) return null;
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        if (user.getRoles() != null) {
            res.setRoles(user.getRoles().stream()
                    .map(RoleEntity::getName)
                    .collect(Collectors.toList()));
        }
        return res;
    }

}
