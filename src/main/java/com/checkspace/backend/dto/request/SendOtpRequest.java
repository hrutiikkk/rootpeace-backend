package com.checkspace.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank(message = "Phone number required")
    private String phone;

    private String email;

    private String role; // BUYER or SELLER
}