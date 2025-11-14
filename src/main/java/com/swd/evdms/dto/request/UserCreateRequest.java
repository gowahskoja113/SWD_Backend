package com.swd.evdms.dto.request;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private Integer roleId; // optional; default staff
}

