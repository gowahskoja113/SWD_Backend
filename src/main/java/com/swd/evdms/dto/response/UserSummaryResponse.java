package com.swd.evdms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummaryResponse {
    private Integer id;
    private String name;
    private String email;
    private String role;
}

