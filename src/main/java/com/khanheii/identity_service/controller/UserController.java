package com.khanheii.identity_service.controller;

import ch.qos.logback.core.model.processor.PhaseIndicator;
import com.khanheii.identity_service.Service.UserService;
import com.khanheii.identity_service.dto.request.ApiResponse;
import com.khanheii.identity_service.dto.request.UserCreationRequest;
import com.khanheii.identity_service.dto.request.UserUpdateRequest;
import com.khanheii.identity_service.dto.response.UserResponse;
import com.khanheii.identity_service.entity.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> getUsers(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username: {}",authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));


        return userService.getUsers();
    }

    @GetMapping("users/{userId}")
    public UserResponse getUser(@PathVariable String userId){

        return userService.getUser(userId);
    }

    @GetMapping("users/myInfo")
    public ApiResponse<UserResponse> getMyInfo(){

        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfor())
                .build();

    }

    @PutMapping("users/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request){

        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("users/{userId}")
    public String deleteUser(@PathVariable String userId){

        userService.deleteUser(userId);
        return "User has been deleted";
    }

}
