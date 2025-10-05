package com.swd.evdms.mapper;

import com.swd.evdms.dto.request.RegisterRequest;
import com.swd.evdms.dto.response.RegisterResponse;
import com.swd.evdms.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        return user;
    }

    public RegisterResponse toDto(User user) {
        RegisterResponse response = new RegisterResponse();
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAddress(user.getAddress());
        response.setRole(user.getRole().getRoleName());
        return response;
    }
}