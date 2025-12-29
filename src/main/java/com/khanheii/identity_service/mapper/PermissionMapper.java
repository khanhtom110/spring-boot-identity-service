package com.khanheii.identity_service.mapper;

import com.khanheii.identity_service.dto.request.PermissionRequest;
import com.khanheii.identity_service.dto.request.UserCreationRequest;
import com.khanheii.identity_service.dto.request.UserUpdateRequest;
import com.khanheii.identity_service.dto.response.PermissionResponse;
import com.khanheii.identity_service.dto.response.UserResponse;
import com.khanheii.identity_service.entity.Permission;
import com.khanheii.identity_service.entity.Role;
import com.khanheii.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission (PermissionRequest request);
    PermissionResponse toPermissionResponse (Permission permission);
}
