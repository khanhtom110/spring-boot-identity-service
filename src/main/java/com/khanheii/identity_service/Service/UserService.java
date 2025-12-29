package com.khanheii.identity_service.Service;

import com.khanheii.identity_service.dto.request.ApiResponse;
import com.khanheii.identity_service.dto.request.UserCreationRequest;
import com.khanheii.identity_service.dto.request.UserUpdateRequest;
import com.khanheii.identity_service.dto.response.UserResponse;
import com.khanheii.identity_service.entity.User;
import com.khanheii.identity_service.enums.Role;
import com.khanheii.identity_service.exception.AppException;
import com.khanheii.identity_service.exception.ErrorCode;
import com.khanheii.identity_service.repository.RoleRepository;
import com.khanheii.identity_service.repository.UserRepository;
import com.khanheii.identity_service.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;


    public UserResponse createUser(UserCreationRequest request){


        if(userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
//        user.setRoles(roles);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfor(){

        var context = SecurityContextHolder.getContext(); //Lay thong tin user dang yeu cau request
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(()->new AppException(ErrorCode.USER_NONEXISTED));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')") //Tim nhung authorities co Role o truoc
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')") //Cung duoc
//    @PreAuthorize("hasAuthority('APPROVE_POST')")  //De lay cac permission
    public ApiResponse getUsers(){
        return ApiResponse.builder()
                .result(userRepository.findAll())
                .build();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id){
        log.info("In method get User");
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(()->new RuntimeException("User not found")));
    }

    public UserResponse updateUser(String id, @RequestBody UserUpdateRequest request){

            User user = userRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.USER_NONEXISTED));

            userMapper.updateUser(user,request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
            return userMapper.toUserResponse(userRepository.save(user));
    }


    public void deleteUser(String id){

        userRepository.deleteById(id);
    }


}
