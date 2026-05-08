package com.datn.shopuser.service;

import com.datn.shopdatabase.entity.PermissionEntity;
import com.datn.shopdatabase.entity.RoleEntity;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopdatabase.repository.PermissionRepository;
import com.datn.shopdatabase.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    public RoleEntity createRole(RoleEntity role) {
        if (roleRepository.existsById(role.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        return roleRepository.save(role);
    }

    @Transactional
    public RoleEntity assignPermissions(String roleName, Set<String> permissionNames) {
        RoleEntity role = roleRepository.findById(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        Set<PermissionEntity> permissions = permissionNames.stream()
                .map(name -> permissionRepository.findById(name)
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED)))
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    public Set<PermissionEntity> getPermissionsByRole(String roleName) {
        RoleEntity role = roleRepository.findById(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        return role.getPermissions();
    }
}