package com.swd.evdms.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String email;

    public AuthResponse(String token, String email) {
        this.token = token;
        this.email = email;
        this.tokenType = "Bearer";
    }
}
