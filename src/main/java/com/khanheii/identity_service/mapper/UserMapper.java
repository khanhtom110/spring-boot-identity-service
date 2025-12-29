package com.khanheii.identity_service.mapper;

import com.khanheii.identity_service.dto.request.UserCreationRequest;
import com.khanheii.identity_service.dto.request.UserUpdateRequest;
import com.khanheii.identity_service.dto.response.UserResponse;
import com.khanheii.identity_service.entity.Role;
import com.khanheii.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    default String mapRoleToString(Role role) {
        if (role == null) {
            return null;
        }
        // 2. Đảm bảo 'role.getName()' là đúng
        // (Có thể là .getCode() hoặc .name() tùy vào class Role của bạn)
        return role.getName();
    }
}
