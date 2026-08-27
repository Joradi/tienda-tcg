package com.tiendatcg.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthLoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    public AuthLoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
