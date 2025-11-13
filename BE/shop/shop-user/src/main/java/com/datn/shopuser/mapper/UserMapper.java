package com.datn.shopuser.mapper;

import com.datn.shopcore.entity.Role;
import com.datn.shopuser.dto.request.UserCreationRequest;
import com.datn.shopuser.dto.request.UserUpdateRequest;
import com.datn.shopuser.dto.response.UserResponse;

import com.datn.shopcore.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    default Set<Role> map(List<String> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(roleName -> {
                    Role role = new Role();
                    role.setName(roleName); // giả sử Role có field 'name'
                    return role;
                })
                .collect(Collectors.toSet());
    }
}
