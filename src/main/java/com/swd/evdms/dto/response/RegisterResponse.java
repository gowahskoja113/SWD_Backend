package com.swd.evdms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
}
