package com.datn.shopadmin.controller;

import com.datn.shopdatabase.dto.ApiResponse;
import com.datn.shopdatabase.entity.PermissionEntity;
import com.datn.shopdatabase.entity.RoleEntity;
import com.datn.shopuser.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RoleEntity>> getAllRoles() {
        return ApiResponse.<List<RoleEntity>>builder()
                .result(roleService.getAllRoles())
                .build();
    }

    @GetMapping("/{roleName}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Set<PermissionEntity>> getRolePermissions(@PathVariable String roleName) {
        return ApiResponse.<Set<PermissionEntity>>builder()
                .result(roleService.getPermissionsByRole(roleName))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleEntity> createRole(@RequestBody RoleEntity role) {
        return ApiResponse.<RoleEntity>builder()
                .result(roleService.createRole(role))
                .build();
    }

    @PutMapping("/{roleName}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleEntity> assignPermissions(@PathVariable String roleName,
                                                     @RequestBody Set<String> permissionNames) {
        return ApiResponse.<RoleEntity>builder()
                .result(roleService.assignPermissions(roleName, permissionNames))
                .build();
    }
}