package com.checkspace.backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String role;
    private Long userId;
    private String phone;
    private boolean newUser;
}